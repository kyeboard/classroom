package me.kyeboard.classroom.screens

import MembersList
import android.graphics.Color
import android.os.Bundle
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import io.appwrite.Client
import io.appwrite.Query
import io.appwrite.services.Account
import io.appwrite.services.Teams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.kyeboard.classroom.R
import me.kyeboard.classroom.utils.getAppwriteClient
import me.kyeboard.classroom.utils.invisible
import me.kyeboard.classroom.utils.visible

val email_check_regex = Regex("^\\w+([.-]?\\w+)*@\\w+([.-]?\\w+)*(\\.\\w{2,})+$")

class ClassPeople : AppCompatActivity() {
    private lateinit var client: Client
    private lateinit var teams: Teams
    private lateinit var classId: String
    private var isOwner: Boolean = false
    private lateinit var adapter: MembersList
    private lateinit var view: RecyclerView
    private lateinit var account: Account
    private lateinit var loading: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        // Set view
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_classppl)

        // Get items from extras
        val extras = intent.extras!!
        classId = extras.getString("class_id")!!
        val accentColor = Color.parseColor(extras.getString("accent_color")!!)

        // Set accent color
        window.statusBarColor = accentColor
        findViewById<ConstraintLayout>(R.id.classppl_topbar).background.mutate().setTint(accentColor)

        // View holders
        loading = findViewById(R.id.members_list_loading)
        view = findViewById(R.id.members_list)
        val refreshLayout = findViewById<SwipeRefreshLayout>(R.id.members_list_refresh_layout)

        // Initiate appwrite services
        client = getAppwriteClient(this)
        teams = Teams(client)
        account = Account(client)

        // Handle refreshing
        refreshLayout.setOnRefreshListener {
            populateList(false) {
                refreshLayout.isRefreshing = false
            }
        }

        // Handle destroy self
        findViewById<ImageView>(R.id.newclass_destory_self).setOnClickListener {
            finish()
        }

        // Initial load
        CoroutineScope(Dispatchers.IO).launch {
            val session = account.get()

            val membership = teams.listMemberships(
                classId,
                arrayListOf(Query.equal("userId", session.id))
            ).memberships[0]

            isOwner = membership.roles.contains("owner")

            runOnUiThread {
                populateList { }
            }
        }

        // Handle sending invite
        findViewById<ImageView>(R.id.send_invite).setOnClickListener {
            val emailAddress = findViewById<EditText>(R.id.classppl_invite_email).text.toString()
            val inviteAsTeacher = findViewById<CheckBox>(R.id.invite_as_teacher).isChecked

            if(!email_check_regex.matches(emailAddress)) {
                Toast.makeText(this, "Invalid email address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Send an invite
            CoroutineScope(Dispatchers.IO).launch {
                val roles = if(inviteAsTeacher) {
                    listOf("owner")
                } else {
                    listOf()
                }

                // Send an invite
                teams.createMembership(classId, emailAddress, roles, "https://classroom.kyeboard.me/accept_invite")

                // Send a toast msg
                runOnUiThread {
                    populateList()

                    Toast.makeText(this@ClassPeople, "Successfully sent an invite!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun populateList(showLoading: Boolean = true, callback: () -> Unit = {  }) {
        if(showLoading) {
            visible(loading)
            invisible(view)
        }

        CoroutineScope(Dispatchers.IO).launch {
            val members = teams.listMemberships(classId).memberships

            adapter = MembersList(members, isOwner, this@ClassPeople::removeUser)

            runOnUiThread {
                invisible(loading)
                visible(view)

                view.adapter = adapter
                view.layoutManager = LinearLayoutManager(this@ClassPeople)

                if(isOwner) {
                    visible(findViewById<RecyclerView>(R.id.invite_new_member_parent))
                }

                callback()
            }
        }
    }

    private fun removeUser(membershipId: String, index: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                teams.deleteMembership(classId, membershipId)

                populateList(true)

                runOnUiThread {
                    Toast.makeText(this@ClassPeople, "Successfully removed the user!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@ClassPeople, "Error while removing the user", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
