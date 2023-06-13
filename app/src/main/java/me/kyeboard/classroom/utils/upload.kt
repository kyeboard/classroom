package me.kyeboard.classroom.utils

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import io.appwrite.models.InputFile
import io.appwrite.models.UploadProgress
import io.appwrite.services.Storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream


fun getMimeType(url: String?): String? {
    var type: String? = null
    val extension = MimeTypeMap.getFileExtensionFromUrl(url)
    if (extension != null) {
        type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    }
    return type
}

/// Copes a input stream to a output stream
@Throws(IOException::class)
fun copyStream(`in`: InputStream, out: OutputStream) {
    // Create a new byte array
    val buffer = ByteArray(1024)

    // Amount of data read
    var read: Int

    // Read
    while (`in`.read(buffer).also { read = it } != -1) {
        out.write(buffer, 0, read)
    }
}

/// Uploads a uri to appwrite storage and returns the id of the file
suspend fun uploadToAppwriteStorage(resolver: ContentResolver, uri: Uri, storage: Storage, bucketId: String = "6465d3dd2e3905c17280"): String {
    val inputStream = resolver.openInputStream(uri)
    val fileName = getFileName(resolver, uri)

    // Create a temporary file
    val file = withContext(Dispatchers.IO) {
        File.createTempFile(fileName, "tmp")
    }

    // Create output stream for the file
    val outputStream = withContext(Dispatchers.IO) {
        FileOutputStream(file)
    }

    // Copy uri stream to output stream
    copyStream(inputStream!!, outputStream)

    val inputFile = InputFile.fromFile(file)

    inputFile.filename = fileName

    // Upload the temporary file to appwrite
    val appwriteFile = storage.createFile(bucketId, "unique()", inputFile)

    // Close input stream after usage
    withContext(Dispatchers.IO) {
        inputStream.close()
    }

    // Return id
    return appwriteFile.id
}

/// Returns the file nam of the uri
fun getFileName(resolver: ContentResolver, uri: Uri): String {
    val returnCursor: Cursor = resolver.query(uri, null, null, null, null)!!
    val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
    returnCursor.moveToFirst()
    val name = returnCursor.getString(nameIndex)
    returnCursor.close()
    return name
}