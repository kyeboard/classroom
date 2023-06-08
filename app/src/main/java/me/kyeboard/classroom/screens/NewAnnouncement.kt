package me.kyeboard.classroom.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.database.Cursor
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import io.appwrite.models.InputFile
import io.appwrite.services.Databases
import io.appwrite.services.Storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.kyeboard.classroom.R
import me.kyeboard.classroom.adapters.Attachment
import me.kyeboard.classroom.adapters.AttachmentAdapter
import me.kyeboard.classroom.adapters.ViewPagerAdapter
import me.kyeboard.classroom.fragments.AnnouncementItem
import me.kyeboard.classroom.fragments.CreateNewAnnouncement
import me.kyeboard.classroom.fragments.CreateNewMeeting
import me.kyeboard.classroom.utils.get_appwrite_client
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

data class AnnouncementItem(val author: String, val message: String, val attachments: ArrayList<String>, val classid: String)

class NewAnnouncement : AppCompatActivity() {
    val attachments: ArrayList<Attachment> = arrayListOf()
    val attachments_uri: ArrayList<Uri> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_newannouncement)

        window.statusBarColor = Color.parseColor("#fee587")

        val adapter = AttachmentAdapter(attachments)
        val class_id = intent.extras!!.getString("class_id")!!
        val recyclerView = findViewById<RecyclerView>(R.id.new_announcement_attachments_list)

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        val client = get_appwrite_client(this)
        val databases = Databases(client)
        val storage = Storage(client)

        val intent = Intent().apply {
            action = Intent.ACTION_GET_CONTENT
            type = "*/*"
        }

        val pickFiles = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if(result.resultCode == Activity.RESULT_OK) {
                val content_uri = result.data?.data!!
                val file_name = getFileName(this.contentResolver, content_uri)

                attachments.add(Attachment(file_name.substringAfterLast('.', ""), file_name))
                attachments_uri.add(content_uri)

                adapter.notifyItemChanged(attachments.size - 1)
            }
        }

        findViewById<Button>(R.id.new_announcement_attach_files).setOnClickListener {
            pickFiles.launch(intent)
        }

        findViewById<Button>(R.id.new_announcement_create_announcement).setOnClickListener {
            val message = findViewById<EditText>(R.id.new_announcement_message).text.toString()

            if(message.isBlank()) {
                Toast.makeText(this, "Fill in all details before submitting", Toast.LENGTH_SHORT).show()

                return@setOnClickListener
            }

            val attachment_ids = arrayListOf<String>()

            CoroutineScope(Dispatchers.IO).launch {
                for(uri in attachments_uri) {
                    attachment_ids.add(uploadToAppwriteStorage(this@NewAnnouncement.contentResolver, uri, storage))
                }

                databases.createDocument("classes", "647c1b704310bb8f0fed", "unique()", AnnouncementItem("kyeboard", message, attachment_ids, class_id!!))
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

    private fun getFileName(resolver: ContentResolver, uri: Uri): String {
        val returnCursor: Cursor = resolver.query(uri, null, null, null, null)!!
        val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        returnCursor.moveToFirst()
        val name = returnCursor.getString(nameIndex)
        returnCursor.close()
        return name
    }
}