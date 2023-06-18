package me.kyeboard.classroom.fragments

import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.appwrite.Query
import io.appwrite.Role
import io.appwrite.extensions.tryJsonCast
import io.appwrite.services.Account
import io.appwrite.services.Databases
import io.appwrite.services.Storage
import io.appwrite.services.Teams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.kyeboard.classroom.R
import me.kyeboard.classroom.adapters.Attachment
import me.kyeboard.classroom.adapters.AttachmentAdapter
import me.kyeboard.classroom.screens.AssignmentItem
import me.kyeboard.classroom.utils.getAppwriteClient
import me.kyeboard.classroom.utils.getFileName
import me.kyeboard.classroom.utils.invisible
import me.kyeboard.classroom.utils.openAttachment
import me.kyeboard.classroom.utils.uploadToAppwriteStorage
import me.kyeboard.classroom.utils.visible
import java.lang.Math.abs
import java.math.BigInteger
import java.security.MessageDigest

data class SubmissionItem(val grade: Int, val submissions: ArrayList<String>, val studentId: String, val studentName: String)

fun hashmd5(target: String): String {
    return BigInteger(1, MessageDigest.getInstance("MD5").digest(target.toByteArray())).toString(16).padStart(32, '0')
}


open class OnSwipeTouchListener(ctx: Context?) : OnTouchListener {
    private val gestureDetector: GestureDetector

    init {
        gestureDetector = GestureDetector(ctx, GestureListener())
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event)
    }

    private inner class GestureListener : SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onFling(
            e1: MotionEvent,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            var result = false
            try {
                val diffY = e2.y - e1.y
                val diffX = e2.x - e1.x
                if (abs(diffX) > abs(diffY)) {
                    if (abs(diffX) > 100 && abs(velocityX) > 100) {
                        if (diffX > 0) {
                            onSwipeRight()
                        } else {
                            onSwipeLeft()
                        }
                        result = true
                    }
                } else if (abs(diffY) > 100 && abs(velocityY) > 100) {
                    if (diffY > 0) {
                        onSwipeBottom()
                    } else {
                        onSwipeTop()
                    }
                    result = true
                }
            } catch (exception: java.lang.Exception) {
                exception.printStackTrace()
            }
            return result
        }

    }

    open fun onSwipeRight() {}
    open fun onSwipeLeft() {}
    open fun onSwipeTop() {}
    open fun onSwipeBottom() {}
}

class NewAssignmentTask : Fragment() {
    private var attachmentUris: ArrayList<Uri> = arrayListOf()
    private val attachments = arrayListOf<Attachment>()
    private var alreadySubmitted = false
    private var unexpandedHeight: Int = 0
    private var isOwner = false
    private lateinit var classId: String

    private lateinit var submissionArea: ScrollView
    private lateinit var view: View
    private lateinit var chevron_down: ImageView
    private lateinit var chevron_up: ImageView
    private lateinit var add_files_btn: AppCompatButton
    private lateinit var submissionList: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        view = inflater.inflate(R.layout.fragment_new_assignment_task, container, false)

        val context = requireContext()

        val client = getAppwriteClient(context)
        val databases = Databases(client)
        val storage = Storage(client)
        val teams = Teams(client)
        val account = Account(client)

        submissionArea = view.findViewById(R.id.assignment_task_submission)
        add_files_btn = view.findViewById(R.id.assignment_view_add_files)
        chevron_up = view.findViewById(R.id.assignment_task_expand_submission_up)
        chevron_down = view.findViewById(R.id.assignment_task_expand_submission_down)
        val loading = view.findViewById<ConstraintLayout>(R.id.assignment_task_loading)
        submissionList = view.findViewById(R.id.assignment_view_submissions_list)

        unexpandedHeight = submissionArea.height

        view.setOnTouchListener(object : OnSwipeTouchListener(context) {
            override fun onSwipeTop() {
                moveViewDown()
            }

            override fun onSwipeBottom() {
                moveViewUp()
            }
        })

        // Arguments
        val args = requireArguments()
        val assignmentId = args.getString("assignment_id")!!
        classId = args.getString("class_id")!!

        // Handle expansion
        chevron_up.setOnClickListener {
            moveViewUp()
        }

        chevron_down.setOnClickListener {
            moveViewDown()
        }


        // Check if the user has already submitted the assignment
        CoroutineScope(Dispatchers.IO).launch {
            val session = account.get()
            val id = "$assignmentId-${session.id}"
            val hashedId = hashmd5(id)

            try {
                val submission = databases.getDocument("classes", "submissions", hashedId).data.tryJsonCast<SubmissionItem>()!!

                alreadySubmitted = true

                val attachments = arrayListOf<Attachment>()

                for(attachment_id in submission.submissions) {
                    val file = storage.getFile("submissions", attachment_id).name
                    attachments.add(Attachment(file.substringAfterLast('.', ""), file))
                }

                requireActivity().runOnUiThread {
                    invisible(loading)
                    add_files_btn.visibility = View.GONE
                    view.findViewById<Button>(R.id.assignment_view_submit_assignment).apply {
                        text = "Already submitted"
                        isEnabled = false
                    }

                    submissionList.apply {
                        adapter = AttachmentAdapter(attachments)
                        layoutManager = GridLayoutManager(this@NewAssignmentTask.context, 2)
                    }
                }
            } catch(e: Exception) {
                requireActivity().runOnUiThread {
                    invisible(loading)
                }
            }
        }

        val description = view.findViewById<TextView>(R.id.assignment_view_description)
        val listview = view.findViewById<RecyclerView>(R.id.assignment_view_attachment_list)
        val submissionsAdapter = AttachmentAdapter(attachments)

        submissionList.adapter = submissionsAdapter
        submissionList.layoutManager = GridLayoutManager(view.context, 2)

        val intent = Intent().apply {
            action = Intent.ACTION_GET_CONTENT
            type = "*/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }

        // Handle file
        val pickFiles = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if(result.resultCode == Activity.RESULT_OK) {
                if(result.data != null) {
                    val clipData = result.data!!.clipData

                    if(clipData != null) {
                        for (index in 0 until clipData.itemCount) {
                            val uri: Uri = clipData.getItemAt(index).uri

                            val fileName = getFileName(requireActivity().contentResolver, uri)
                            attachments.add(Attachment(fileName.substringAfterLast('.', ""), fileName))
                            attachmentUris.add(uri)
                        }

                        submissionsAdapter.notifyItemRangeChanged(attachments.size - 1, clipData.itemCount)
                    } else {
                        val contentsURI = result.data?.data!!
                        val fileName = getFileName(requireActivity().contentResolver, contentsURI)

                        attachments.add(Attachment(fileName.substringAfterLast('.', ""), fileName))
                        attachmentUris.add(contentsURI)

                        submissionsAdapter.notifyItemChanged(attachments.size - 1)
                    }
                }
            }
        }

        // Add file launcher handler
        view.findViewById<Button>(R.id.assignment_view_add_files).setOnClickListener {
            pickFiles.launch(intent)
        }

        // Handle submitting assignment
        view.findViewById<Button>(R.id.assignment_view_submit_assignment).setOnClickListener {
            Toast.makeText(this.context, "Submitting your assignment...", Toast.LENGTH_SHORT).show()

            CoroutineScope(Dispatchers.IO).launch {
                val currentUser = account.get()
                val id = "$assignmentId-${currentUser.id}"
                val hashedId = BigInteger(1, MessageDigest.getInstance("MD5").digest(id.toByteArray())).toString(16).padStart(32, '0')

                val attachmentsIds = arrayListOf<String>()

                for(uri in attachmentUris) {
                    attachmentsIds.add(uploadToAppwriteStorage(view.context.contentResolver, uri, storage, "submissions"))
                }

                databases.createDocument(
                    "classes",
                    "submissions",
                    hashedId,
                    SubmissionItem(0, attachmentsIds, currentUser.id, currentUser.name),
                    arrayListOf(
                        io.appwrite.Permission.read(Role.user(currentUser.id)),
                        io.appwrite.Permission.read(Role.team(classId, "owner")),
                    )
                )

                activity?.runOnUiThread {
                    Toast.makeText(this@NewAssignmentTask.context, "Successfully submitted the assignment!", Toast.LENGTH_LONG).show()
                }
            }
        }

        // Load assignment
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val assignmentItem = databases.getDocument("classes", "assignments", assignmentId)
                val data = assignmentItem.data.tryJsonCast<AssignmentItem>()!!
                val attachments = arrayListOf<Attachment>()
                val session = account.get()

                isOwner = teams.listMemberships(classId, arrayListOf(Query.equal("userId", session.id))).memberships[0].roles.contains("owner")

                for(attachment_id in data.attachments) {
                    val file = storage.getFile("attachments", attachment_id)

                    val attachment = Attachment(file.name.substringAfterLast('.', ""), file.name)  {
                        // Handle on click
                        requireActivity().runOnUiThread {
                            Toast.makeText(view.context, "Please wait while the file is being downloaded", Toast.LENGTH_SHORT).show()
                        }
                        CoroutineScope(Dispatchers.IO).launch {
                            startActivity(openAttachment(view.context, storage, attachment_id, file.name, file.mimeType))
                        }
                    }

                    attachments.add(attachment)
                }

                activity?.runOnUiThread {
                    description.text = data.description
                    visible(description)
                    visible(listview)
                    if(isOwner) {
                        visible(submissionArea)
                    }

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

    private fun moveViewUp() {
        animateHeightTo(unexpandedHeight)

        invisible(view.findViewById<AppCompatButton>(R.id.assignment_view_add_files))
        invisible(submissionList)
        
        chevron_up.animate().alpha(0f).duration = 100
        chevron_up.translationZ = 0f
        chevron_down.animate().alpha(1f).duration = 100
        chevron_down.translationZ = 1f
    }
    
    private fun animateHeightTo(to: Int) {
        val valueAnimator = ValueAnimator.ofInt(submissionArea.height, to)
        valueAnimator.duration = 150

        valueAnimator.addUpdateListener { animator ->
            val layoutParams = submissionArea.layoutParams
            layoutParams.height = animator.animatedValue as Int
            submissionArea.layoutParams = layoutParams
        }

        valueAnimator.start()
    }

    private fun moveViewDown() {
        animateHeightTo(600)

        if(!alreadySubmitted) {
            add_files_btn.visibility = View.VISIBLE
            add_files_btn.animate().alpha(1f)
        }
        submissionList.alpha = 0f
        visible(submissionList)
        submissionList.animate().alpha(1f)

        chevron_up.animate().alpha(1f).duration = 100
        chevron_up.translationZ = 1f
        chevron_down.animate().alpha(0f).duration = 100
        chevron_down.translationZ = 0f
    }
}