package me.kyeboard.classroom.screens

import Comment
import CommentAdapter
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.appwrite.extensions.tryJsonCast
import io.appwrite.services.Account
import io.appwrite.services.Databases
import io.appwrite.services.Storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.kyeboard.classroom.R
import me.kyeboard.classroom.adapters.Attachment
import me.kyeboard.classroom.adapters.AttachmentAdapter
import me.kyeboard.classroom.utils.getAppwriteClient
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
        (findViewById<ConstraintLayout>(R.id.submission_view_topbar).background.mutate() as GradientDrawable).setTint(Color.parseColor(accentColor))

        val client = getAppwriteClient(this)
        val databases = Databases(client)
        val account = Account(client)
        val storage = Storage(client)

        val attachmentView = findViewById<RecyclerView>(R.id.submission_view_attachment_list)
        val commentsView = findViewById<RecyclerView>(R.id.submission_view_comments_list)

        findViewById<ImageView>(R.id.submission_view_msg_send).setOnClickListener {
            val msg = findViewById<EditText>(R.id.submission_view_msg_textbox).text.toString()

            CoroutineScope(Dispatchers.IO).launch {
                val session = account.get()
                val submissionDetails = databases.getDocument("classes", "submissions", getID(assignmentId, submissionId))
                val currentComments = submissionDetails.data.tryJsonCast<SubmissionItem>()!!.comments

                currentComments.add("{\"author\": \"${session.name}\", \"message\": \"$msg\"}")

                databases.updateDocument(
                    "classes",
                    "submissions", getID(assignmentId, submissionId),
                    UpdateItem(currentComments)
                )

                updateData(databases, storage, assignmentId, submissionId, commentsView, attachmentView)
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            updateData(databases, storage, assignmentId, submissionId, commentsView, attachmentView)
        }
    }

    private suspend fun updateData(databases: Databases, storage: Storage, assignmentId: String, submissionId: String, commentsView: RecyclerView, attachmentView: RecyclerView) {
        val submissionDetails = databases.getDocument("classes", "submissions", getID(assignmentId, submissionId))
        val attachmentList = arrayListOf<Attachment>()
        val data = submissionDetails.data.tryJsonCast<SubmissionItem>()!!

        for(i in data.submissions) {
            val file_name = storage.getFile("submissions", i).name
            attachmentList.add(Attachment(file_name.substringAfterLast('.', ""), file_name))
        }

        runOnUiThread {
        }
        val commentList = arrayListOf<Comment>()

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
