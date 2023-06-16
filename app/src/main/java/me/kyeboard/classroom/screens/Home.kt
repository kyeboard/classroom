package me.kyeboard.classroom.screens

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import io.appwrite.Client
import io.appwrite.extensions.tryJsonCast
import io.appwrite.services.Account
import io.appwrite.services.Databases
import io.appwrite.services.Teams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.kyeboard.classroom.R
import me.kyeboard.classroom.adapters.ClassItem
import me.kyeboard.classroom.adapters.ClassesListAdapter
import me.kyeboard.classroom.utils.getAppwriteClient
import me.kyeboard.classroom.utils.invisible
import me.kyeboard.classroom.utils.loadUserSession
import me.kyeboard.classroom.utils.logoutAndRedirect
import me.kyeboard.classroom.utils.visible

class Home : AppCompatActivity() {
    private lateinit var client: Client
    private lateinit var account: Account
    private lateinit var databases: Databases
    private lateinit var teams: Teams
    private lateinit var listview: RecyclerView
    private lateinit var loading: ProgressBar
    private lateinit var noClassesParent: ConstraintLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        // Initialize view
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Setup appwrite services
        client = getAppwriteClient(applicationContext)
        account = Account(client)
        databases = Databases(client)
        teams = Teams(client)

        // Initialize view holders
        val refreshView = findViewById<SwipeRefreshLayout>(R.id.home_pull_to_refresh)
        noClassesParent = findViewById(R.id.no_classes_found_parent)
        listview = findViewById(R.id.home_classes_list)
        loading = findViewById(R.id.home_classes_list_loading)

        // Set colors
        window.statusBarColor = ResourcesCompat.getColor(resources, R.color.yellow, theme)

        // Handle logout
        findViewById<ImageView>(R.id.logout_user).setOnClickListener {
            logoutAndRedirect(account, this)
        }

        // Setup view
        listview.layoutManager = LinearLayoutManager(applicationContext)

        // Handle refreshing layout when the new class finishes creating a new class
        val handler = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if(it.resultCode == Activity.RESULT_OK) {
                populateClassesList {  }
            }
        }

        // Handler refresh layout event
        refreshView.setOnRefreshListener {
            populateClassesList {
                refreshView.isRefreshing = false
            }
        }

        // Load user session
        loadUserSession(this, account, R.id.current_user_name, R.id.current_user_email, R.id.current_user_profile)

        // Initial class list load
        populateClassesList()

        // Handle new class popup
        findViewById<ImageButton>(R.id.open_new_class_popup).setOnClickListener {
            // Create a new intent
            val intent = Intent(this, NewClass::class.java)

            // Start the intent
            handler.launch(intent)
        }
    }

    private fun populateClassesList(callback: () -> Unit = {  }) {
        visible(loading)
        listview.animate().alpha(0f).duration = 100

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Get the data
                val classes = teams.list().teams.map {
                    databases.getDocument("classes", "registry", it.id).data.tryJsonCast<ClassItem>()!!
                }

                // Configure recycler view
                runOnUiThread {
                    // If there are no teams, show the no found widget
                    if(classes.isEmpty()) {
                        visible(noClassesParent)
                    } else {
                        invisible(noClassesParent)
                    }

                    invisible(loading)
                    visible(listview)
                    listview.animate().alpha(1f).duration = 100

                    // Add the adapter
                    listview.adapter = ClassesListAdapter(classes, this@Home::openClassDashboard, this@Home)

                    callback()
                }
            } catch(e: Exception) {
                Log.e("populate_class_list_error", e.message.toString())
            }
        }
    }

    private fun openClassDashboard(id: String, accent_color: String) {
        // Create a new intent for the dashboard
        val intent = Intent(this@Home, ClassDashboard::class.java)

        // Put the id
        intent.putExtra("class_id", id)
        intent.putExtra("accent_color", accent_color)

        // Start
        startActivity(intent)
    }
}