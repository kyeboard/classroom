package me.kyeboard.classroom.screens

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Button
import android.widget.EditText
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

data class Assignment(val title: String, val description: String, val attachments: ArrayList<String>, val author: String, val grade: Number, val due_date: String, val classid: String)

class NewAssignment : ComponentActivity() {
    val attachments: ArrayList<Attachment> = arrayListOf()
    val attachments_uri: ArrayList<Uri> = arrayListOf()

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
        setContentView(R.layout.activity_newassignment)

        window.statusBarColor = android.graphics.Color.parseColor("#fee587")

        val adapter = AttachmentAdapter(attachments)
        val recyclerView = findViewById<RecyclerView>(R.id.new_assignment_attachments)
        //val class_id = intent.extras!!.getString("class_id")!!
        val class_id = ""

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        val client = get_appwrite_client(this)
        val database = Databases(client)
        val storage = Storage(client)

        val intent = Intent().apply {
            action = Intent.ACTION_GET_CONTENT
            type = "*/*"
        }

        val pickFiles = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if(result.resultCode == Activity.RESULT_OK) {
                val content_uri = result.data?.data!!
                val file_name = getFileName(contentResolver, content_uri)

                attachments.add(Attachment(file_name.substringAfterLast('.', ""), file_name))
                attachments_uri.add(content_uri)

                adapter.notifyItemChanged(attachments.size - 1)
            }
        }

        findViewById<Button>(R.id.new_assignment_add_attachment).setOnClickListener {
            pickFiles.launch(intent)
        }

        findViewById<Button>(R.id.newassignment_create_assignment).setOnClickListener {
            val title = findViewById<EditText>(R.id.newassignment_title).text.toString()
            val desc = findViewById<EditText>(R.id.newassignment_description).text.toString()
            val grade = findViewById<EditText>(R.id.newassignment_grade).text.toString().toInt()
            val duedate = findViewById<EditText>(R.id.newassignment_duedate).text.toString()

            if(title.isBlank() || desc.isBlank() || duedate.isBlank()) {
                Toast.makeText(this, "Make sure to fill all the details", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val attachment_ids = arrayListOf<String>()

            CoroutineScope(Dispatchers.IO).launch {
                for(uri in attachments_uri) {
                    attachment_ids.add(uploadToAppwriteStorage(contentResolver, uri, storage))
                }

                database.createDocument("classes", "646f432ad59caafabf74", "unique()",
                    Assignment(title, desc, attachment_ids, "kyeboard", grade, duedate, class_id)
                )
            }
        }
    }

    @Throws(IOException::class)
    fun copyStream(`in`: InputStream, out: OutputStream) {
        val buffer = ByteArray(1024)
        var read: Int
        while (`in`.read(buffer).also { read = it } != -1) {
            out.write(buffer, 0, read)
        }
    }

    private suspend fun uploadToAppwriteStorage(resolver: ContentResolver, uri: Uri, storage: Storage): String {
        val input_stream = resolver.openInputStream(uri)
        val file_name = getFileName(resolver, uri)

        val file = File.createTempFile(file_name, "tmp")
        val output_stream = FileOutputStream(file)

        copyStream(input_stream!!, output_stream)

        val appwrite_file = storage.createFile("6465d3dd2e3905c17280", "unique()", InputFile.fromFile(file))

        return appwrite_file.id
    }

}
