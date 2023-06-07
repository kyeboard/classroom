package me.kyeboard.classroom.screens

import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
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

data class Announcement(val author: String, val message: String, val attachments: ArrayList<String>)

class AnnouncementView : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_announcementview)

        window.statusBarColor = Color.parseColor("#fee587")

        //val announcement_id = intent.extras!!.getString("announcement_id")!!
        // Picasso.get().load("https://cloud.appwrite.io/v1/storage/buckets/646ef17593d213adfcf2/files/647dd94e7155f82fb365/view?project=fryday").into(findViewById<ImageView>(R.id.announcement_item_author_pfp))
        val announcement_id = "647f2e27affb3ca71756"
        val client = get_appwrite_client(this)
        val database = Databases(client)
        val storage = Storage(client)

        val username = findViewById<TextView>(R.id.announcement_item_username)
        val time = findViewById<TextView>(R.id.announcement_item_time)
        val description = findViewById<TextView>(R.id.announcement_itemview_description)
        val view = findViewById<RecyclerView>(R.id.announcement_itemview_attachment_list)

        CoroutineScope(Dispatchers.IO).launch {
            val announcement_obj = database.getDocument("classes", "647c1b704310bb8f0fed", announcement_id)
            val data = announcement_obj.data.tryJsonCast<Announcement>()!!
            val attachments = arrayListOf<Attachment>()

            for(attachment_id in data.attachments) {
                val file = storage.getFile("6465d3dd2e3905c17280", attachment_id).name
                attachments.add(Attachment(file.substringAfterLast('.', ""), file))
            }

            runOnUiThread {
                username.text = data.author
                time.text = announcement_obj.createdAt

                description.text = data.message

                view.adapter = AttachmentAdapter(attachments)
                view.layoutManager = LinearLayoutManager(this@AnnouncementView)
            }
        }
    }
}
