package me.kyeboard.classroom.screens

import MembersList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import io.appwrite.Client
import io.appwrite.services.Teams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.kyeboard.classroom.R
import me.kyeboard.classroom.screens.ui.theme.ClassroomTheme
import me.kyeboard.classroom.utils.get_appwrite_client

val email_check_regex = Regex("^\\w+([.-]?\\w+)*@\\w+([.-]?\\w+)*(\\.\\w{2,})+$")

class ClassPeople : ComponentActivity() {
    private lateinit var client: Client
    private lateinit var teams: Teams
    private lateinit var classId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        // Set view
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_classppl)

        actionBar?.hide()

        // Get items from extras
        val extras = intent.extras!!
        classId = extras.getString("class_id")!!
        val accentColor = extras.getString("accent_color")!!

        window.statusBarColor = Color.parseColor(accentColor)

        // View holders
        val loading = findViewById<ProgressBar>(R.id.members_list_loading)
        val view = findViewById<RecyclerView>(R.id.members_list)
        val refreshLayout = findViewById<SwipeRefreshLayout>(R.id.members_list_refresh_layout)

        // Set accent
        findViewById<ConstraintLayout>(R.id.classppl_topbar).background.setTint(Color.parseColor(accentColor))

        // Initiate appwrite services
        client = get_appwrite_client(applicationContext)
        teams = Teams(client)

        // Handle refreshing
        refreshLayout.setOnRefreshListener {
            CoroutineScope(Dispatchers.IO).launch {
                populateList(view, loading, false)
                refreshLayout.isRefreshing = false
            }
        }

        // Handle destroy self
        findViewById<ImageView>(R.id.newclass_destory_self).setOnClickListener {
            finish()
        }

        // Initial load
        CoroutineScope(Dispatchers.IO).launch { populateList(view, loading) }

        // Handle sending invite
        findViewById<ImageView>(R.id.send_invite).setOnClickListener {
            val email_address = findViewById<EditText>(R.id.classppl_invite_email).text.toString()
            val invite_as_teacher = findViewById<CheckBox>(R.id.invite_as_teacher).isChecked

            if(email_address.isBlank()) {
                Toast.makeText(this, "Email address cannot be empty!", Toast.LENGTH_SHORT).show()

                return@setOnClickListener
            }

            if(!email_check_regex.matches(email_address)) {
                Toast.makeText(this, "Invalid email address", Toast.LENGTH_SHORT).show()

                return@setOnClickListener
            }

            // Send an invite
            CoroutineScope(Dispatchers.IO).launch {
                val roles = if(invite_as_teacher) {
                    listOf("teacher")
                } else {
                    listOf()
                }

                // Send an invite
                teams.createMembership(classId, email_address, roles, "https://fryday.vercel.app")

                // Refresh layout
                populateList(view, loading)

                // Send a toast msg
                runOnUiThread {
                    Toast.makeText(this@ClassPeople, "Successfully sent an invite!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun populateList(view: RecyclerView, loading: ProgressBar, showLoading: Boolean = true) {
        runOnUiThread {
            if(showLoading) {
                loading.visibility = View.VISIBLE
                view.visibility = View.GONE
            }
        }

        val members = teams.listMemberships(classId).memberships

        runOnUiThread {
            loading.visibility = View.GONE
            view.visibility = View.VISIBLE

            view.adapter = MembersList(members)
            view.layoutManager = LinearLayoutManager(this@ClassPeople)
        }
    }
}
