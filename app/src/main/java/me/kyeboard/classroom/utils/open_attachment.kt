package me.kyeboard.classroom.utils

import android.content.Context
import android.content.Intent
import android.os.Environment
import androidx.core.content.FileProvider
import io.appwrite.services.Storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

public suspend fun openAttachment(context: Context, storage: Storage, file_id: String, file_name: String, mimeType: String): Intent {
    // Download the file and save it
    val directory = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
    val file = File(directory, "${file_name.substringBeforeLast('.')}-${file_id}.${file_name.substringAfterLast('.')}")

    if(!file.exists()) {
        val fileForDownload = storage.getFileDownload("attachments", file_id)

        // Write the stream to file
        withContext(Dispatchers.IO) {
            val stream = FileOutputStream(file)
            stream.write(fileForDownload)
            stream.close()
        }
    }

    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
    intent.setDataAndType(uri, mimeType)
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    return intent
}