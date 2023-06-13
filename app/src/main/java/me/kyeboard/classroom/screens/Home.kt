package me.kyeboard.classroom.screens

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.squareup.picasso.Picasso
import io.appwrite.Client
import io.appwrite.extensions.tryJsonCast
import io.appwrite.services.Account
import io.appwrite.services.Databases
import io.appwrite.services.Storage
import io.appwrite.services.Teams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.kyeboard.classroom.R
import me.kyeboard.classroom.adapters.ClassItem
import me.kyeboard.classroom.adapters.ClassesListAdapter
import me.kyeboard.classroom.utils.get_appwrite_client

class Home : AppCompatActivity() {
    private lateinit var client: Client
    private lateinit var account: Account
    private lateinit var databases: Databases
    private lateinit var teams: Teams
    private lateinit var listview: RecyclerView
    private lateinit var loading: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        // Initialize view
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Remove status bar (make it transparent)
        WindowCompat.setDecorFitsSystemWindows(
            window,
            false
        )

        // Setup appwrite services
        client = get_appwrite_client(applicationContext)
        account = Account(client)
        databases = Databases(client)
        teams = Teams(client)

        // Handle logout
        findViewById<ImageView>(R.id.logout_user).setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                // Delete current session
                account.deleteSession("current")

                // Send toast
                runOnUiThread {
                    Toast.makeText(applicationContext, "Successfully logged out!", Toast.LENGTH_SHORT).show()
                }

                // Redirect to login activity
                val login = Intent(this@Home, Login::class.java)
                startActivity(login)
                finish()
            }
        }

        // View holders
        val noClassesParent = findViewById<ConstraintLayout>(R.id.no_classes_found_parent)
        val refreshView = findViewById<SwipeRefreshLayout>(R.id.home_pull_to_refresh)
        listview = findViewById(R.id.home_classes_list)
        loading = findViewById(R.id.home_classes_list_loading)

        // Setup view
        listview.layoutManager = LinearLayoutManager(applicationContext)

        // Handle refreshing layout when the new class finishes creating a new class
        val handler = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if(it.resultCode == Activity.RESULT_OK) {
                CoroutineScope(Dispatchers.IO).launch {
                    populateClassesList(noClassesParent)
                }
            }
        }

        // Handler refresh layout event
        refreshView.setOnRefreshListener {
            CoroutineScope(Dispatchers.IO).launch {
                populateClassesList(noClassesParent, false)

                refreshView.isRefreshing = false
            }
        }

        // Load current info
        CoroutineScope(Dispatchers.IO).launch {
            // Show current user info
            try {
                val session = account.get()

                runOnUiThread {
                    findViewById<TextView>(R.id.current_user_name).text = session.name
                    findViewById<TextView>(R.id.current_user_email).text = session.email

                    Picasso
                        .get()
                        .load("https://cloud.appwrite.io/v1/storage/buckets/646ef17593d213adfcf2/files/${session.id}/view?project=fryday")
                        .into(findViewById<ImageView>(R.id.current_user_profile))
                }
            } catch(e: Exception) {
                // User has some issue with session so its better for a login
                val intent = Intent(this@Home, Login::class.java)

                // Start
                startActivity(intent)

                // Finish current since there is no more usage of it
                finish()
            }

            // Get the list of the teams that the current user is in
            try {
                populateClassesList(noClassesParent)
            } catch(e: Exception) {
                Log.e("populate_classes_list", e.toString())
            }
        }

        // Handle new class popup
        findViewById<ImageButton>(R.id.open_new_class_popup).setOnClickListener {
            // Create a new intent
            val intent = Intent(this, NewClass::class.java)

            // Start the intent
            handler.launch(intent)
        }
    }

    private suspend fun populateClassesList(noClassesParent: ConstraintLayout, showLoading: Boolean = true) {
        // Get the data
        val classes = teams.list().teams
        val userClasses = arrayListOf<ClassItem>()

        runOnUiThread {
            noClassesParent.visibility = View.GONE
            listview.visibility = View.GONE

            if(showLoading) {
                loading.visibility = View.VISIBLE
            }
        }

        // Iterate over each team
        for (team in classes) {
            // Get register
            val item = databases.getDocument("classes", "registery", team.id).data.tryJsonCast<ClassItem>()!!

            // Set total students
            item.total = team.total

            // Add to list
            userClasses.add(item)
        }

        // Configure recycler view
        runOnUiThread {
            // If there are no teams, show the no found widget
            if(classes.isEmpty()) {
                noClassesParent.visibility = View.VISIBLE
            }

            loading.visibility = View.GONE
            listview.visibility = View.VISIBLE

            // Add the adapter
            listview.adapter = ClassesListAdapter(userClasses, this@Home::openClassDashboard, this@Home)
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