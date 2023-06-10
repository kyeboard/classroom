package me.kyeboard.classroom.screens

import MembersList
import android.graphics.Color
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.appwrite.services.Teams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.kyeboard.classroom.R
import me.kyeboard.classroom.screens.ui.theme.OxideTheme
import me.kyeboard.classroom.utils.get_appwrite_client

class ClassPeople : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_classppl)

        actionBar?.hide()

        val classId = intent.extras!!.getString("class_id")!!
        val accentColor = intent.extras!!.getString("accent_color")!!

        window.statusBarColor = Color.parseColor(accentColor)
        findViewById<ConstraintLayout>(R.id.classppl_topbar).background.setTint(Color.parseColor(accentColor))

        val view = findViewById<RecyclerView>(R.id.members_list)
        val client = get_appwrite_client(this)
        val teams = Teams(client)

        val email_check_regex = Regex("^\\w+([.-]?\\w+)*@\\w+([.-]?\\w+)*(\\.\\w{2,})+$")

        findViewById<ImageView>(R.id.send_invite).setOnClickListener {
            val email_address = findViewById<EditText>(R.id.classppl_invite_email).text.toString()

            if(email_address.isBlank()) {
                Toast.makeText(this, "Email address cannot be empty!", Toast.LENGTH_SHORT).show()

                return@setOnClickListener
            }

            if(!email_check_regex.matches(email_address)) {
                Toast.makeText(this, "Invalid email address", Toast.LENGTH_SHORT).show()

                return@setOnClickListener
            }

            // Send an invite
            GlobalScope.launch {
                teams.createMembership(classId, email_address, listOf(), "https://fryday.vercel.app")

                runOnUiThread {
                    Toast.makeText(this@ClassPeople, "Successfully sent an invite!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            val members = teams.listMemberships(classId).memberships

            runOnUiThread {
                view.adapter = MembersList(members)
                view.layoutManager = LinearLayoutManager(this@ClassPeople)
            }
        }
    }
}
