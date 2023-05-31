package me.kyeboard.classroom.screens

import AssignmentAdapter
import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.appwrite.extensions.tryJsonCast
import io.appwrite.models.InputFile
import io.appwrite.services.Databases
import io.appwrite.services.Storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.kyeboard.classroom.R
import me.kyeboard.classroom.adapters.Attachment
import me.kyeboard.classroom.adapters.AttachmentAdapter
import me.kyeboard.classroom.screens.ui.theme.OxideTheme
import me.kyeboard.classroom.utils.get_appwrite_client
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.math.BigInteger
import java.security.MessageDigest
import kotlin.text.Charsets.UTF_8

data class AssignmentItem(val title: String, val description: String, val attachments: ArrayList<String>, val author: String, val grade: Int, val due_date: String)

class AssignmentView : ComponentActivity() {
    private lateinit var attachment_uri: Uri
    private val attachments = arrayListOf<Attachment>()

    @Throws(IOException::class)
    fun copyStream(`in`: InputStream, out: OutputStream) {
        val buffer = ByteArray(1024)
        var read: Int
        while (`in`.read(buffer).also { read = it } != -1) {
            out.write(buffer, 0, read)
        }
    }

    private suspend fun uploadToAppwriteStorage(id: String, resolver: ContentResolver, uri: Uri, storage: Storage): String {
        val input_stream = resolver.openInputStream(uri)
        val file_name = getFileName(resolver, uri)

        val file = File.createTempFile(file_name, "tmp")
        val output_stream = FileOutputStream(file)

        copyStream(input_stream!!, output_stream)

        val appwrite_file = storage.createFile("647713fa9be2a68d4458", id, InputFile.fromFile(file))

        return appwrite_file.id
    }

    private fun getFileName(resolver: ContentResolver, uri: Uri): String {
        val returnCursor: Cursor = resolver.query(uri, null, null, null, null)!!
        val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        returnCursor.moveToFirst()
        val name = returnCursor.getString(nameIndex)
        returnCursor.close()
        return name
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assignmentview)

        val assignment_id = "646f5e9c8ba221bf2df1"

        val client = get_appwrite_client(this)
        val databases = Databases(client)
        val storage = Storage(client)

        val title = findViewById<TextView>(R.id.assignment_view_title)
        val author = findViewById<TextView>(R.id.assignment_view_author)
        val description = findViewById<TextView>(R.id.assignment_view_description)
        val listview = findViewById<RecyclerView>(R.id.assignment_view_attachment_list)
        val sumbissionlist = findViewById<RecyclerView>(R.id.assignment_view_submissions_list)
        val sumbissionsAdapter = AttachmentAdapter(attachments)

        sumbissionlist.adapter = sumbissionsAdapter
        sumbissionlist.layoutManager = LinearLayoutManager(this)

        val intent = Intent().apply {
            action = Intent.ACTION_GET_CONTENT
            type = "*/*"
        }

        val pickFiles = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if(result.resultCode == Activity.RESULT_OK) {
                val content_uri = result.data?.data!!
                val file_name = getFileName(this.contentResolver, content_uri)

                if(attachments.size == 0) {
                    attachments.add(Attachment(file_name.substringAfterLast('.', ""), file_name))
                } else {
                    attachments[0] = Attachment(file_name.substringAfterLast('.', ""), file_name)
                }
                attachment_uri = content_uri

                sumbissionsAdapter.notifyItemChanged(attachments.size - 1)
            }
        }

        findViewById<Button>(R.id.assignment_view_add_files).setOnClickListener {
            pickFiles.launch(intent)
        }

        findViewById<Button>(R.id.assignment_view_submit_assignment).setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                val id = "$assignment_id-64735c4d22a9a24664e8"
                val hashed_id = BigInteger(1, MessageDigest.getInstance("MD5").digest(id.toByteArray())).toString(16).padStart(32, '0')
                uploadToAppwriteStorage(hashed_id, this@AssignmentView.contentResolver, attachment_uri, storage)
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val assignment_item = databases.getDocument("classes", "646f432ad59caafabf74", assignment_id)
                val data = assignment_item.data.tryJsonCast<AssignmentItem>()!!
                val attachments = arrayListOf<Attachment>()

                for(attachment_id in data.attachments) {
                    val file = storage.getFile("6465d3dd2e3905c17280", attachment_id).name
                    attachments.add(Attachment(file.substringAfterLast('.', ""), file))
                }

                runOnUiThread {
                    title.text = data.title
                    author.text = data.author
                    description.text = data.description

                    listview.adapter = AttachmentAdapter(attachments)
                    listview.layoutManager = LinearLayoutManager(this@AssignmentView)
                }
            } catch(e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@AssignmentView, e.message.toString(), Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}
