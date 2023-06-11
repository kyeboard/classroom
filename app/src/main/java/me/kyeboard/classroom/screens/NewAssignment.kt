package me.kyeboard.classroom.screens

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.database.Cursor
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.appwrite.models.InputFile
import io.appwrite.services.Account
import io.appwrite.services.Databases
import io.appwrite.services.Storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.kyeboard.classroom.R
import me.kyeboard.classroom.adapters.Attachment
import me.kyeboard.classroom.adapters.AttachmentAdapter
import me.kyeboard.classroom.screens.ui.theme.OxideTheme
import me.kyeboard.classroom.utils.getFileName
import me.kyeboard.classroom.utils.get_appwrite_client
import me.kyeboard.classroom.utils.uploadToAppwriteStorage
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

data class Assignment(val title: String, val description: String, val attachments: ArrayList<String>, val author: String, val grade: Number, val due_date: String, val classid: String, val authorId: String)

class NewAssignment : ComponentActivity() {
    val attachments: ArrayList<Attachment> = arrayListOf()
    val attachments_uri: ArrayList<Uri> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Setup view
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_newassignment)

        // Get items from bundle
        val class_id = intent.extras!!.getString("class_id")!!
        val accent_color = intent.extras!!.getString("accent_color")!!

        // View items
        val recyclerView = findViewById<RecyclerView>(R.id.new_assignment_attachments)

        // Set accent
        (findViewById<ConstraintLayout>(R.id.newassignment_topbar).background as GradientDrawable).apply {
            setTint(android.graphics.Color.parseColor(accent_color))
        }
        window.statusBarColor = android.graphics.Color.parseColor(accent_color)
        (findViewById<Button>(R.id.newassignment_create_assignment).background as GradientDrawable).apply {
            setColor(android.graphics.Color.parseColor(accent_color))
            setStroke(5, android.graphics.Color.parseColor("#000000"))
            cornerRadius = 5F
        }

        // Adapters
        val adapter = AttachmentAdapter(attachments)

        // Set finish listener
        findViewById<ImageView>(R.id.destroy_self).setOnClickListener {
            finish()
        }

        // Add adapters
        recyclerView.adapter = adapter
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        // Initialize appwrite services
        val client = get_appwrite_client(this)
        val database = Databases(client)
        val storage = Storage(client)
        val account = Account(client)

        // Handle input
        val intent = Intent().apply {
            action = Intent.ACTION_GET_CONTENT
            type = "*/*"
        }

        // handler
        val pickFiles = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if(result.resultCode == Activity.RESULT_OK) {
                val content_uri = result.data?.data!!
                val file_name = getFileName(contentResolver, content_uri)

                attachments.add(Attachment(file_name.substringAfterLast('.', ""), file_name))
                attachments_uri.add(content_uri)

                adapter.notifyItemChanged(attachments.size - 1)
            }
        }

        // Add attachment listener
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
                val user = account.get()

                for(uri in attachments_uri) {
                    attachment_ids.add(uploadToAppwriteStorage(contentResolver, uri, storage))
                }

                database.createDocument("classes", "646f432ad59caafabf74", "unique()",
                    Assignment(title, desc, attachment_ids, user.name, grade, duedate, class_id, user.id)
                )

                setResult(Activity.RESULT_OK)

                finish()
            }
        }
    }
}
