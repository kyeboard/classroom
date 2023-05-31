package me.kyeboard.classroom.screens

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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

data class AssignmentItem(val title: String, val description: String, val attachments: ArrayList<String>, val author: String, val grade: Int, val due_date: String)

class AssignmentView : ComponentActivity() {
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
