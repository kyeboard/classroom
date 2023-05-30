package me.kyeboard.classroom.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
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
import me.kyeboard.classroom.utils.get_appwrite_client
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CreateNewAnnouncement.newInstance] factory method to
 * create an instance of this fragment.
 */

data class AnnouncementItem(val author: String, val description: String, val attachments: ArrayList<String>)

class CreateNewAnnouncement : Fragment() {
    val attachments: ArrayList<Attachment> = arrayListOf()
    val attachments_uri: ArrayList<Uri> = arrayListOf()

    @Throws(IOException::class)
    fun copyStream(`in`: InputStream, out: OutputStream) {
        val buffer = ByteArray(1024)
        var read: Int
        while (`in`.read(buffer).also { read = it } != -1) {
            out.write(buffer, 0, read)
        }
    }

    @SuppressLint("Range")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.create_new_announcement, container, false)

        val adapter = AttachmentAdapter(attachments)
        val recyclerView = view.findViewById<RecyclerView>(R.id.new_announcement_attachments_list)

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(view.context)

        val client = get_appwrite_client(view.context)
        val databases = Databases(client)
        val storage = Storage(client)

        val intent = Intent().apply {
            action = Intent.ACTION_GET_CONTENT
            type = "*/*"
        }

        val pickFiles = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if(result.resultCode == Activity.RESULT_OK) {
                val content_uri = result.data?.data!!
                val file_name = getFileName(view.context.contentResolver, content_uri)

                attachments.add(Attachment(file_name.substringAfterLast('.', ""), file_name))
                attachments_uri.add(content_uri)

                adapter.notifyItemChanged(attachments.size - 1)
            }
        }

        view.findViewById<Button>(R.id.new_announcement_attach_files).setOnClickListener {
            pickFiles.launch(intent)
        }

        view.findViewById<Button>(R.id.new_announcement_create_announcement).setOnClickListener {
            val message = view.findViewById<EditText>(R.id.new_announcement_message).text.toString()

            if(message.isBlank()) {
                Toast.makeText(view.context, "Fill in all details before submitting", Toast.LENGTH_SHORT).show()

                return@setOnClickListener
            }

            val attachment_ids = arrayListOf<String>()

            CoroutineScope(Dispatchers.IO).launch {
                for(uri in attachments_uri) {
                    attachment_ids.add(uploadToAppwriteStorage(view.context.contentResolver, uri, storage))
                }

                databases.createDocument("classes", "646c532bc46aecc1120a", "unique()", AnnouncementItem("kyeboard", message, attachment_ids))
            }
        }

        return view
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

    private fun getFileName(resolver: ContentResolver, uri: Uri): String {
        val returnCursor: Cursor = resolver.query(uri, null, null, null, null)!!
        val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        returnCursor.moveToFirst()
        val name = returnCursor.getString(nameIndex)
        returnCursor.close()
        return name
    }
}