package me.kyeboard.classroom.screens

import Comment
import CommentAdapter
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.kyeboard.classroom.R
import me.kyeboard.classroom.adapters.Attachment
import me.kyeboard.classroom.adapters.AttachmentAdapter
import me.kyeboard.classroom.screens.ui.theme.OxideTheme
import me.kyeboard.classroom.utils.get_appwrite_client
import org.json.JSONObject
import java.math.BigInteger
import java.security.MessageDigest

data class SubmissionItem(val grade: Number, val comments: ArrayList<String>, val submissions: ArrayList<String>)
data class UpdateItem(val comments: ArrayList<String>)

class SubmissionView : ComponentActivity() {
    private fun getID(assignment_id: String, submission_id: String): String {
        val md = MessageDigest.getInstance("MD5")
        return BigInteger(1, md.digest("$assignment_id-$submission_id".toByteArray())).toString(16).padStart(32, '0')
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_submissionview)

        /*
        val client = get_appwrite_client(this)
        val databases = Databases(client)
        val storage = Storage(client)

        val attachment_view = findViewById<RecyclerView>(R.id.submission_view_attachment_list)
        val comments_view = findViewById<RecyclerView>(R.id.submission_view_comments_list)

        val assignmment_id = "6475d05340bf25c2069b"
        val submission_id = "6475d05340bf25c2069b"

        findViewById<Button>(R.id.submission_view_msg_send).setOnClickListener {
            val msg = findViewById<EditText>(R.id.submission_view_msg_textbox).text.toString()

            GlobalScope.launch {
                val submission_details = databases.getDocument("classes", "64782b0c5957666e7bee", getID(assignmment_id, submission_id))
                val current_comments = submission_details.data.tryJsonCast<SubmissionItem>()!!.comments

                current_comments.add("{\"author\": \"kyeboard\", \"message\": \"$msg\"}0")

                databases.updateDocument(
                    "classes",
                    "64782b0c5957666e7bee", getID(assignmment_id, submission_id),
                    UpdateItem(current_comments)
                )
            }
        }

        GlobalScope.launch {
            val submission_details = databases.getDocument("classes", "64782b0c5957666e7bee", getID(assignmment_id, submission_id))
            val attachment_list = arrayListOf<Attachment>()
            val comment_list = arrayListOf<Comment>()
            val data = submission_details.data.tryJsonCast<SubmissionItem>()!!

            for(i in data.submissions) {
                val file_name = storage.getFile("647713fa9be2a68d4458", i).name
                attachment_list.add(Attachment(file_name.substringAfterLast('.', ""), file_name))
            }

            for(comment in data.comments) {
                val parsed = JSONObject(comment)
                val comment = Comment(parsed.get("author").toString(), parsed.get("message").toString())

                comment_list.add(comment)
            }

            runOnUiThread {
                attachment_view.adapter = AttachmentAdapter(attachment_list)
                attachment_view.layoutManager = LinearLayoutManager(this@SubmissionView)

                comments_view.adapter = CommentAdapter(comment_list)
                comments_view.layoutManager = LinearLayoutManager(this@SubmissionView)
            }
        } */
    }
}
