package me.kyeboard.classroom.screens

import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.appwrite.Client
import io.appwrite.extensions.tryJsonCast
import io.appwrite.services.Databases
import io.appwrite.services.Storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.kyeboard.classroom.R
import me.kyeboard.classroom.adapters.Attachment
import me.kyeboard.classroom.adapters.AttachmentAdapter
import me.kyeboard.classroom.utils.getAppwriteClient
import me.kyeboard.classroom.utils.imageInto
import me.kyeboard.classroom.utils.invisible
import me.kyeboard.classroom.utils.openAttachment
import me.kyeboard.classroom.utils.setText
import java.text.SimpleDateFormat
import java.util.Locale

data class Announcement(val author: String, val message: String, val attachments: ArrayList<String>, val userId: String)

// Date forms
val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.US)
val outputFormat = SimpleDateFormat("d MMMM, yyyy", Locale.US)

class AnnouncementView : ComponentActivity() {
    private lateinit var client: Client
    private lateinit var databases: Databases
    private lateinit var storage: Storage

    override fun onCreate(savedInstanceState: Bundle?) {
        // Setup view
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_announcementview)

        // Get args
        val bundle = intent.extras!!
        val announcementId = bundle.getString("announcement_id")!!
        val accentColor = bundle.getString("accent_color")!!

        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Set accent color
        findViewById<ConstraintLayout>(R.id.announcement_view_topbar).background.mutate().setTint(Color.parseColor(accentColor))

        // Initialize appwrite services
        client = getAppwriteClient(applicationContext)
        databases = Databases(client)
        storage = Storage(client)

        // Handle maniacs
        findViewById<ImageView>(R.id.destroy_self).setOnClickListener {
            finish()
        }

        // Get the views
        val view = findViewById<RecyclerView>(R.id.announcement_itemview_attachment_list)

        // Get the data and populate views
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Get the announcement
                val announcementObj = databases.getDocument("classes", "647c1b704310bb8f0fed", announcementId)

                // Get the data and parse it
                val data = announcementObj.data.tryJsonCast<Announcement>()!!

                // List of attachments
                val attachments = arrayListOf<Attachment>()

                // Get the files attached
                for(attachment_id in data.attachments) {
                    // Get the file
                    val file = storage.getFile("6465d3dd2e3905c17280", attachment_id)

                    // Add it to the list
                    attachments.add(Attachment(file.name.substringAfterLast('.', ""), file.name) {
                        // Handle on click
                        runOnUiThread {
                            Toast.makeText(this@AnnouncementView, "Please wait while the file is being downloaded", Toast.LENGTH_SHORT).show()
                        }
                        CoroutineScope(Dispatchers.IO).launch {
                            startActivity(openAttachment(applicationContext, storage, attachment_id, file.name, file.mimeType))
                        }
                    })
                }

                // Set the items to the view
                runOnUiThread {
                    invisible(findViewById<ProgressBar>(R.id.announcement_view_loading))
                    setText(this@AnnouncementView, R.id.announcement_view_author, data.author)
                    setText(this@AnnouncementView, R.id.announcement_view_time, outputFormat.format(inputFormat.parse(announcementObj.createdAt)!!))

                    // Load pfp
                    imageInto(this@AnnouncementView, "https://cloud.appwrite.io/v1/storage/buckets/646ef17593d213adfcf2/files/${data.userId}/view?project=fryday", R.id.announcement_item_author_pfp)

                    setText(this@AnnouncementView, R.id.announcement_itemview_description, data.message)

                    view.adapter = AttachmentAdapter(attachments)
                    view.layoutManager = GridLayoutManager(this@AnnouncementView, 2)
                }
            } catch(e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@AnnouncementView, "Cannot find the announcement", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
