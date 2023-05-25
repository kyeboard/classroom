package me.kyeboard.classroom.screens

import Chat
import ChatsAdapter
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.appwrite.extensions.tryJsonCast
import io.appwrite.services.Databases
import io.appwrite.services.Realtime
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.kyeboard.classroom.R
import me.kyeboard.classroom.utils.get_appwrite_client

class MeetingChat : ComponentActivity() {
    val messages: ArrayList<Chat> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.meeting_chat)

        val client = get_appwrite_client(this)
        val realtime = Realtime(client)
        val databases = Databases(client)

        val view = findViewById<RecyclerView>(R.id.meeting_chat_view)

        view.adapter = ChatsAdapter(messages)
        view.layoutManager = LinearLayoutManager(this)

        GlobalScope.launch {
            for(message in databases.listDocuments("classes", "646cc4c8aab6656a1b16").documents) {
                messages.add(message.data.tryJsonCast()!!)
            }

            runOnUiThread { this@MeetingChat.update_view(view) }
        }

        realtime.subscribe("databases.classes.collections.646cc4c8aab6656a1b16.documents") {
            messages.add(it.payload.tryJsonCast<Chat>()!!)

            Log.d("messages_service", messages.toString())

            this.update_view(view)
        }
    }

    private fun update_view(view: RecyclerView) {
        try {
            runOnUiThread {
                view.adapter = ChatsAdapter(messages)
                view.adapter?.notifyDataSetChanged()
            }
        } catch (e: Exception) {
            Log.e("dfdf", e.message.toString())
        }
    }
}
