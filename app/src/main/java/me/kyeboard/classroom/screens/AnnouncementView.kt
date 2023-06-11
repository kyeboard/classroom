package me.kyeboard.classroom.screens

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
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
import me.kyeboard.classroom.utils.get_appwrite_client
import java.text.SimpleDateFormat
import java.util.Locale

data class Announcement(val author: String, val message: String, val attachments: ArrayList<String>, val userId: String)

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
        val announcement_id = bundle.getString("announcement_id")!!
        val accent_color = bundle.getString("accent_color")!!

        // Set accent color
        window.statusBarColor = Color.parseColor(accent_color)
        findViewById<ConstraintLayout>(R.id.announcement_view_topbar).background.setTint(Color.parseColor(accent_color))

        // Initialize appwrite services
        client = get_appwrite_client(applicationContext)
        databases = Databases(client)
        storage = Storage(client)

        // Get the views
        val description = findViewById<TextView>(R.id.announcement_itemview_description)
        val view = findViewById<RecyclerView>(R.id.announcement_itemview_attachment_list)
        val pfp = findViewById<ImageView>(R.id.announcement_item_author_pfp)

        // Get the data and populate views
        CoroutineScope(Dispatchers.IO).launch {
            // Get the announcement
            val announcement_obj = databases.getDocument("classes", "647c1b704310bb8f0fed", announcement_id)

            // Get the data and parse it
            val data = announcement_obj.data.tryJsonCast<Announcement>()!!

            // List of attachments
            val attachments = arrayListOf<Attachment>()

            // Get the files attached
            for(attachment_id in data.attachments) {
                val file = storage.getFile("6465d3dd2e3905c17280", attachment_id).name
                attachments.add(Attachment(file.substringAfterLast('.', ""), file))
            }

            // Set the items to the view
            runOnUiThread {
                findViewById<ProgressBar>(R.id.announcement_view_loading).visibility = View.GONE
                findViewById<TextView>(R.id.announcement_view_author).text = data.author
                findViewById<TextView>(R.id.announcement_view_time).text = outputFormat.format(inputFormat.parse(announcement_obj.createdAt)!!)

                Picasso.get().load("https://cloud.appwrite.io/v1/storage/buckets/646ef17593d213adfcf2/files/${data.userId}/view?project=fryday").into(pfp)

                description.text = data.message

                view.adapter = AttachmentAdapter(attachments)
                view.layoutManager = LinearLayoutManager(this@AnnouncementView)
            }
        }
    }
}
