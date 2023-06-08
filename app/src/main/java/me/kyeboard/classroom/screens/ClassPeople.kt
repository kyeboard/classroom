package me.kyeboard.classroom.screens

import MembersList
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.appwrite.services.Teams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.kyeboard.classroom.R
import me.kyeboard.classroom.screens.ui.theme.OxideTheme
import me.kyeboard.classroom.utils.get_appwrite_client

class ClassPeople : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_classppl)
        
        window.statusBarColor = ResourcesCompat.getColor(resources, R.color.yellow, null)

        actionBar?.hide()

        val classId = "647da6bb4463f64ae8f5"

        val view = findViewById<RecyclerView>(R.id.members_list)
        val client = get_appwrite_client(this)
        val teams = Teams(client)

        CoroutineScope(Dispatchers.IO).launch {
            val members = teams.listMemberships(classId).memberships

            runOnUiThread {
                view.adapter = MembersList(members)
                view.layoutManager = LinearLayoutManager(this@ClassPeople)
            }
        }
    }
}
