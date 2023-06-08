package me.kyeboard.classroom.fragments

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
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
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
import me.kyeboard.classroom.utils.get_appwrite_client
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.math.BigInteger
import java.security.MessageDigest

data class SubmissionItem(val grade: Int, val submissions: ArrayList<String>)

class NewAssignmentTask : Fragment() {
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_new_assignment_task, container, false)

        //val assignment_id = requireArguments().getString("assignment_id")!!
        val assignment_id = "646f5e9c8ba221bf2df1"


        val client = get_appwrite_client(view.context)
        val databases = Databases(client)
        val storage = Storage(client)
        val account = Account(client)

        CoroutineScope(Dispatchers.IO).launch {
            val session = account.get()
            val id = "$assignment_id-${session.id}"
            val hashed_id = BigInteger(1, MessageDigest.getInstance("MD5").digest(id.toByteArray())).toString(16).padStart(32, '0')

            try {
                val submission = databases.getDocument("classes", "64782b0c5957666e7bee", hashed_id).data.tryJsonCast<SubmissionItem>()!!

                val attachments = arrayListOf<Attachment>()

                for(attachment_id in submission.submissions) {
                    val file = storage.getFile("647713fa9be2a68d4458", attachment_id).name
                    attachments.add(Attachment(file.substringAfterLast('.', ""), file))
                }

                requireActivity().runOnUiThread {
                    Toast.makeText(this@NewAssignmentTask.context, "You have already submitted bruh", Toast.LENGTH_SHORT).show()

                    view.findViewById<Button>(R.id.assignment_view_add_files).visibility = View.GONE
                    view.findViewById<Button>(R.id.assignment_view_submit_assignment).apply {
                        text = "Already submitted"
                        isEnabled = false
                    }

                    view.findViewById<RecyclerView>(R.id.assignment_view_submissions_list).apply {
                        adapter = AttachmentAdapter(attachments)
                        layoutManager = LinearLayoutManager(this@NewAssignmentTask.context)
                    }
                }
            } catch(e: Exception) {
                Log.e("ee", e.message.toString())
            }
        }

        val description = view.findViewById<TextView>(R.id.assignment_view_description)
        val listview = view.findViewById<RecyclerView>(R.id.assignment_view_attachment_list)
        val sumbissionlist = view.findViewById<RecyclerView>(R.id.assignment_view_submissions_list)
        val sumbissionsAdapter = AttachmentAdapter(attachments)

        sumbissionlist.adapter = sumbissionsAdapter
        sumbissionlist.layoutManager = LinearLayoutManager(view.context)

        val intent = Intent().apply {
            action = Intent.ACTION_GET_CONTENT
            type = "*/*"
        }

        val pickFiles = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if(result.resultCode == Activity.RESULT_OK) {
                val content_uri = result.data?.data!!
                val file_name = getFileName(view.context.contentResolver, content_uri)

                if(attachments.size == 0) {
                    attachments.add(Attachment(file_name.substringAfterLast('.', ""), file_name))
                } else {
                    attachments[0] = Attachment(file_name.substringAfterLast('.', ""), file_name)
                }
                attachment_uri = content_uri

                sumbissionsAdapter.notifyItemChanged(attachments.size - 1)
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
                val attachments = uploadToAppwriteStorage("unique()", view.context.contentResolver, attachment_uri, storage)

                databases.createDocument("classes", "64782b0c5957666e7bee", hashed_id, SubmissionItem(0, arrayListOf(attachments)))
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

                    listview.adapter = AttachmentAdapter(attachments)
                    listview.layoutManager = LinearLayoutManager(view.context)
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