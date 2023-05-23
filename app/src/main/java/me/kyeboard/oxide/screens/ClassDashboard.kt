package me.kyeboard.oxide.screens

import Announcement
import AnnouncementsAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import io.appwrite.services.Databases
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.kyeboard.oxide.R
import me.kyeboard.oxide.utils.get_appwrite_client

class ClassDashboard : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_classdashboard)

        val view = findViewById<RecyclerView>(R.id.class_dashboard_stream_announcements)
        val client = get_appwrite_client(this)
        val databases = Databases(client)

        findViewById<FloatingActionButton>(R.id.class_dashboard_new_announcement).setOnClickListener {
            val intent = Intent(this, NewAnnouncement::class.java)
            startActivity(intent)
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val data = databases.listDocuments("classes", "646c532bc46aecc1120a").documents
                val adapter = AnnouncementsAdapter(data, this@ClassDashboard::open_announcement_activity)

                runOnUiThread {
                    view.adapter = adapter
                    view.layoutManager = LinearLayoutManager(this@ClassDashboard)
                }
            } catch(e: Exception) {
                Log.e("eee", e.message.toString())
            }
        }
    }

    fun open_announcement_activity(id: String): Unit {

    }
}
