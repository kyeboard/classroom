package me.kyeboard.classroom.screens

import Comment
import CommentAdapter
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.GridLayoutManager
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
import me.kyeboard.classroom.utils.get_appwrite_client
import org.json.JSONObject
import java.math.BigInteger
import java.security.MessageDigest

data class SubmissionItem(val grade: Number, val comments: ArrayList<String>, val submissions: ArrayList<String>)
data class UpdateItem(val comments: ArrayList<String>)

class SubmissionView : ComponentActivity() {
    /// Gens a string that is a MD5 has of two strings
    private fun getID(assignment_id: String, submission_id: String): String {
        val md = MessageDigest.getInstance("MD5")
        return BigInteger(1, md.digest("$assignment_id-$submission_id".toByteArray())).toString(16).padStart(32, '0')
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_submissionview)

        // Get items from bundle
        val extras = intent.extras!!
        val accentColor = extras.getString("accent_color")!!
        val assignmentId = extras.getString("assignment_id")!!
        val submissionId = extras.getString("user_id")!!

        window.statusBarColor = Color.parseColor(accentColor)

        val client = get_appwrite_client(this)
        val databases = Databases(client)
        val storage = Storage(client)

        val attachmentView = findViewById<RecyclerView>(R.id.submission_view_attachment_list)
        val commentsView = findViewById<RecyclerView>(R.id.submission_view_comments_list)

        findViewById<ImageView>(R.id.submission_view_msg_send).setOnClickListener {
            val msg = findViewById<EditText>(R.id.submission_view_msg_textbox).text.toString()

            CoroutineScope(Dispatchers.IO).launch {
                val submissionDetails = databases.getDocument("classes", "64782b0c5957666e7bee", getID(assignmentId, submissionId))
                val currentComments = submissionDetails.data.tryJsonCast<SubmissionItem>()!!.comments

                currentComments.add("{\"author\": \"kyeboard\", \"message\": \"$msg\"}")

                databases.updateDocument(
                    "classes",
                    "64782b0c5957666e7bee", getID(assignmentId, submissionId),
                    UpdateItem(currentComments)
                )
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            val submissionDetails = databases.getDocument("classes", "64782b0c5957666e7bee", getID(assignmentId, submissionId))
            val attachmentList = arrayListOf<Attachment>()
            val commentList = arrayListOf<Comment>()
            val data = submissionDetails.data.tryJsonCast<SubmissionItem>()!!

            for(i in data.submissions) {
                Log.d("tt", i)
                val file_name = storage.getFile("647713fa9be2a68d4458", i).name
                attachmentList.add(Attachment(file_name.substringAfterLast('.', ""), file_name))
            }

            for(comment in data.comments) {
                val parsed = JSONObject(comment)
                val comment = Comment(parsed.get("author").toString(), parsed.get("message").toString())

                commentList.add(comment)
            }

            runOnUiThread {
                attachmentView.adapter = AttachmentAdapter(attachmentList)
                attachmentView.layoutManager = GridLayoutManager(this@SubmissionView, 2)

                commentsView.adapter = CommentAdapter(commentList)
                commentsView.layoutManager = LinearLayoutManager(this@SubmissionView)
            }
        }
    }
}
