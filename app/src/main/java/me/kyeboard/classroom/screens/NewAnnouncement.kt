package me.kyeboard.classroom.screens

import android.app.Activity
import android.app.DatePickerDialog.OnDateSetListener
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.appwrite.Client
import io.appwrite.Permission
import io.appwrite.Query
import io.appwrite.Role
import io.appwrite.services.Account
import io.appwrite.services.Databases
import io.appwrite.services.Storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.kyeboard.classroom.R
import me.kyeboard.classroom.adapters.Attachment
import me.kyeboard.classroom.adapters.AttachmentAdapter
import me.kyeboard.classroom.utils.getFileName
import me.kyeboard.classroom.utils.getAppwriteClient
import me.kyeboard.classroom.utils.uploadToAppwriteStorage
import me.kyeboard.classroom.utils.visible
import java.util.Date


data class AnnouncementItem(val author: String, val message: String, val attachments: ArrayList<String>, val classid: String, val userId: String)

class NewAnnouncement : AppCompatActivity() {
    private val attachments: ArrayList<Attachment> = arrayListOf()
    private val uris: ArrayList<Uri> = arrayListOf()

    private lateinit var client: Client
    private lateinit var databases: Databases
    private lateinit var storage: Storage
    private lateinit var account: Account

    override fun onCreate(savedInstanceState: Bundle?) {
        // Setup view
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_newannouncement)

        // Hide status bar
        WindowCompat.setDecorFitsSystemWindows(
            window,
            false
        )

        // Handle maniacs
        findViewById<ImageView>(R.id.destroy_self).setOnClickListener {
            finish()
        }

        // Get items from bundle
        val extras = intent.extras!!
        val classId = extras.getString("class_id")!!
        val accentColor = extras.getString("accent_color")!!

        // Set the accent
        applyAccent(accentColor)

        // Create adapter for attachments
        val adapter = AttachmentAdapter(attachments)
        val recyclerView = findViewById<RecyclerView>(R.id.new_announcement_attachments_list)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        // Initiate appwrite services
        client = getAppwriteClient(this)
        databases = Databases(client)
        storage = Storage(client)
        account = Account(client)

        // Intent to input files
        val intent = Intent().apply {
            action = Intent.ACTION_GET_CONTENT
            type = "*/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }

        // Handle picking ip files
        val pickFiles = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if(result.resultCode == Activity.RESULT_OK) {
                if(result.data != null) {
                    val clipData = result.data!!.clipData

                    if(clipData != null) {
                        for (index in 0 until clipData.itemCount) {
                            val uri: Uri = clipData.getItemAt(index).uri

                            val fileName = getFileName(this.contentResolver, uri)
                            attachments.add(Attachment(fileName.substringAfterLast('.', ""), fileName))
                            uris.add(uri)
                        }

                        adapter.notifyItemRangeChanged(attachments.size - 1, clipData.itemCount)
                    } else {
                        val contentsURI = result.data?.data!!
                        val fileName = getFileName(this.contentResolver, contentsURI)

                        attachments.add(Attachment(fileName.substringAfterLast('.', ""), fileName))
                        uris.add(contentsURI)

                        adapter.notifyItemChanged(attachments.size - 1)
                    }
                }
            }
        }

        // Launch intent on Add Attachment button click
        findViewById<Button>(R.id.new_announcement_attach_files).setOnClickListener {
            pickFiles.launch(intent)
        }

        // Handle creation of announcement
        findViewById<Button>(R.id.new_announcement_create_announcement).setOnClickListener {
            val message = findViewById<EditText>(R.id.new_announcement_message).text.toString()

            if(message.isBlank()) {
                Toast.makeText(this, "Fill in all details before submitting", Toast.LENGTH_SHORT).show()

                return@setOnClickListener
            }

            val attachmentIds = arrayListOf<String>()
            val loading = findViewById<ConstraintLayout>(R.id.new_announcement_loading_screen);

            loading.alpha = 0f
            visible(loading)
            loading.animate().alpha(1f).duration = 200

            CoroutineScope(Dispatchers.IO).launch {
                for(uri in uris) {
                    attachmentIds.add(uploadToAppwriteStorage(this@NewAnnouncement.contentResolver, uri, storage))
                }

                val currentUser = account.get()

                val res = databases.createDocument(
                    "classes",
                    "announcements",
                    "unique()",
                    AnnouncementItem(
                        currentUser.name,
                        message,
                        attachmentIds,
                        classId,
                        currentUser.id
                    ),
                    arrayListOf(Permission.read(Role.team(classId)))
                )

                setResult(Activity.RESULT_OK)

                // Open the announcement view page
                val announcement = Intent(applicationContext, AnnouncementView::class.java)

                announcement.putExtra("announcement_id", res.id)
                announcement.putExtra("accent_color", accentColor)

                startActivity(announcement)

                finish()
            }
        }
    }

    private fun applyAccent(accentColor: String){
        // Set the accent color
        findViewById<ConstraintLayout>(R.id.newannouncement_topbar).background.apply {
            setTint(Color.parseColor(accentColor))
        }

        // Set the button accent theme
        val bg = findViewById<Button>(R.id.new_announcement_create_announcement).background.mutate() as GradientDrawable

        bg.setColor(Color.parseColor(accentColor))
        bg.setStroke(5, Color.parseColor("#000000"))
        bg.cornerRadius = 5F
    }
}