package me.kyeboard.oxide.screens

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import io.appwrite.extensions.tryJsonCast
import io.appwrite.services.Databases
import io.appwrite.services.Realtime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.kyeboard.oxide.R
import me.kyeboard.oxide.adapters.MeetingMembersAdapter
import me.kyeboard.oxide.screens.ui.theme.OxideTheme
import me.kyeboard.oxide.utils.get_appwrite_client

data class MeetingInfo(val meeting_title: String, val members: List<String>)
data class Member(val name: String, val pfp: String)

class Meeting : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meeting)

        val meeting_id = "6468c28cab23470ff3f9"
        val meeting_title = findViewById<TextView>(R.id.meeting_title)

        val client = get_appwrite_client(this)
        val databases = Databases(client)
        val realtime = Realtime(client)

        realtime.subscribe("databases.classes.collections.6468c0db2c67f3c6c493.documents.6468c28cab23470ff3f9") {
            CoroutineScope(Dispatchers.IO).launch {
                this@Meeting.update_members(databases, meeting_id, meeting_title)
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            this@Meeting.update_members(databases, meeting_id, meeting_title)
        }
    }

    private suspend fun update_members(databases: Databases, meeting_id: String, meeting_title: TextView) {
        val meeting_info = databases.getDocument("classes", "6468c0db2c67f3c6c493", meeting_id).data.tryJsonCast<MeetingInfo>()!!
        val members = arrayListOf<Member>()

        for (member in meeting_info.members) {
            members.add(
                databases.getDocument("classes", "6468c51c09b7cdb9d0b7", member).data.tryJsonCast<Member>()!!
            )
        }

        runOnUiThread {
            meeting_title.text = meeting_info.meeting_title

            val members_list = findViewById<RecyclerView>(R.id.meeting_members)
            members_list.layoutManager = GridLayoutManager(this@Meeting, 2)
            members_list.adapter = MeetingMembersAdapter(members as ArrayList<Any>)
        }
    }
}
