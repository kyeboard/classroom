package me.kyeboard.classroom.fragments

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.appwrite.extensions.tryJsonCast
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
import me.kyeboard.classroom.screens.AssignmentItem
import me.kyeboard.classroom.utils.getFileName
import me.kyeboard.classroom.utils.get_appwrite_client
import me.kyeboard.classroom.utils.invisible
import me.kyeboard.classroom.utils.uploadToAppwriteStorage
import me.kyeboard.classroom.utils.visible
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.math.BigInteger
import java.security.MessageDigest

data class SubmissionItem(val grade: Int, val submissions: ArrayList<String>, val studentId: String, val studentName: String)

fun hashmd5(target: String): String {
    return BigInteger(1, MessageDigest.getInstance("MD5").digest(target.toByteArray())).toString(16).padStart(32, '0')
}

class NewAssignmentTask : Fragment() {
    private var attachment_uris: ArrayList<Uri> = arrayListOf()
    private val attachments = arrayListOf<Attachment>()
    private var already_submitted = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_new_assignment_task, container, false)

        val assignment_id = requireArguments().getString("assignment_id")!!

        val client = get_appwrite_client(view.context)
        val databases = Databases(client)
        val storage = Storage(client)
        val account = Account(client)

        val submission_area = view.findViewById<ConstraintLayout>(R.id.assignment_task_submission)
        val loading = view.findViewById<ConstraintLayout>(R.id.assignment_task_loading)

        view.findViewById<ImageView>(R.id.assignment_task_expand_submission).setOnClickListener {
            val initialHeight = submission_area.height

            if(initialHeight == 1200) {
                val targetHeight = 540 // Increase the height by 200 pixels
                val valueAnimator = ValueAnimator.ofInt(initialHeight, targetHeight)
                valueAnimator.duration = 100 // Animation duration in milliseconds

                valueAnimator.addUpdateListener { animator ->
                    val layoutParams = submission_area.layoutParams
                    layoutParams.height = animator.animatedValue as Int
                    submission_area.layoutParams = layoutParams
                }

                view.findViewById<AppCompatButton>(R.id.assignment_view_add_files).visibility = View.GONE
                view.findViewById<RecyclerView>(R.id.assignment_view_submissions_list).visibility = View.GONE

                valueAnimator.start()
            } else {
                val targetHeight = 1200 // Increase the height by 200 pixels
                val valueAnimator = ValueAnimator.ofInt(initialHeight, targetHeight)
                valueAnimator.duration = 100 // Animation duration in milliseconds

                valueAnimator.addUpdateListener { animator ->
                    val layoutParams = submission_area.layoutParams
                    layoutParams.height = animator.animatedValue as Int
                    submission_area.layoutParams = layoutParams
                }

                valueAnimator.start()

                if(!already_submitted) {
                    view.findViewById<AppCompatButton>(R.id.assignment_view_add_files).visibility = View.VISIBLE
                }
                view.findViewById<RecyclerView>(R.id.assignment_view_submissions_list).visibility = View.VISIBLE
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            val session = account.get()
            val id = "$assignment_id-${session.id}"
            val hashed_id = hashmd5(id)

            try {
                val submission = databases.getDocument("classes", "64782b0c5957666e7bee", hashed_id).data.tryJsonCast<SubmissionItem>()!!

                already_submitted = true

                val attachments = arrayListOf<Attachment>()

                for(attachment_id in submission.submissions) {
                    val file = storage.getFile("647713fa9be2a68d4458", attachment_id).name
                    attachments.add(Attachment(file.substringAfterLast('.', ""), file))
                }

                requireActivity().runOnUiThread {
                    invisible(loading)
                    view.findViewById<Button>(R.id.assignment_view_add_files).visibility = View.GONE
                    view.findViewById<Button>(R.id.assignment_view_submit_assignment).apply {
                        text = "Already submitted"
                        isEnabled = false
                    }

                    view.findViewById<RecyclerView>(R.id.assignment_view_submissions_list).apply {
                        adapter = AttachmentAdapter(attachments)
                        layoutManager = GridLayoutManager(this@NewAssignmentTask.context, 2)
                    }
                }
            } catch(e: Exception) {
                requireActivity().runOnUiThread {
                    invisible(loading)
                }

                Log.e("ee", e.message.toString())
            }
        }

        val description = view.findViewById<TextView>(R.id.assignment_view_description)
        val listview = view.findViewById<RecyclerView>(R.id.assignment_view_attachment_list)
        val sumbissionlist = view.findViewById<RecyclerView>(R.id.assignment_view_submissions_list)
        val submissionsAdapter = AttachmentAdapter(attachments)

        sumbissionlist.adapter = submissionsAdapter
        sumbissionlist.layoutManager = GridLayoutManager(view.context, 2)

        val intent = Intent().apply {
            action = Intent.ACTION_GET_CONTENT
            type = "*/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }

        val pickFiles = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if(result.resultCode == Activity.RESULT_OK) {
                if(result.data != null) {
                    val clipData = result.data!!.clipData

                    if(clipData != null) {
                        for (index in 0 until clipData.itemCount) {
                            val uri: Uri = clipData.getItemAt(index).uri

                            val fileName = getFileName(requireActivity().contentResolver, uri)
                            attachments.add(Attachment(fileName.substringAfterLast('.', ""), fileName))
                            attachment_uris.add(uri)
                        }

                        submissionsAdapter.notifyItemRangeChanged(attachments.size - 1, clipData.itemCount)
                    } else {
                        val contentsURI = result.data?.data!!
                        val fileName = getFileName(requireActivity().contentResolver, contentsURI)

                        attachments.add(Attachment(fileName.substringAfterLast('.', ""), fileName))
                        attachment_uris.add(contentsURI)

                        submissionsAdapter.notifyItemChanged(attachments.size - 1)
                    }
                }
            }
        }

        view.findViewById<Button>(R.id.assignment_view_add_files).setOnClickListener {
            pickFiles.launch(intent)
        }

        view.findViewById<Button>(R.id.assignment_view_submit_assignment).setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                val current_user = account.get()
                val id = "$assignment_id-${current_user.id}"
                val hashed_id = BigInteger(1, MessageDigest.getInstance("MD5").digest(id.toByteArray())).toString(16).padStart(32, '0')

                val attachments_ids = arrayListOf<String>()

                for(uri in attachment_uris) {
                    attachments_ids.add(uploadToAppwriteStorage(view.context.contentResolver, uri, storage, "647713fa9be2a68d4458"))
                }

                databases.createDocument("classes", "64782b0c5957666e7bee", hashed_id, SubmissionItem(0, attachments_ids, current_user.id, current_user.name))

                activity?.runOnUiThread {
                    Toast.makeText(this@NewAssignmentTask.context, "Successfully sumbitted the assignment", Toast.LENGTH_LONG).show()
                }
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

                activity?.runOnUiThread {
                    description.text = data.description
                    visible(description)
                    visible(listview)
                    visible(submission_area)

                    listview.adapter = AttachmentAdapter(attachments)
                    listview.layoutManager = GridLayoutManager(view.context, 2)
                }
            } catch(e: Exception) {
                activity?.runOnUiThread {
                    Toast.makeText(view.context, e.message.toString(), Toast.LENGTH_LONG).show();
                }
            }
        }

        return view
    }
}