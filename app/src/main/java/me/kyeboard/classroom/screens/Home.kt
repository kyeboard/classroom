package me.kyeboard.classroom.screens

import android.app.Activity
import android.content.Intent
import android.os.Bundle
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.squareup.picasso.Picasso
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
import me.kyeboard.classroom.utils.get_appwrite_client

class Home : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Initialize view
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Status bar color
        window.statusBarColor = ResourcesCompat.getColor(resources, R.color.yellow, theme)

        // Setup appwrite services
        val client = get_appwrite_client(this)
        val account = Account(client)
        val database = Databases(client)
        val teamsService = Teams(client)

        // View holders
        val noClassesParent = findViewById<ConstraintLayout>(R.id.no_classes_found_parent)

        // Handle refreshing layout when the new class finishes creating a new class
        val handler = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if(it.resultCode == Activity.RESULT_OK) {
                CoroutineScope(Dispatchers.IO).launch {
                    populateClassesList(teamsService, database, noClassesParent)
                }
            }
        }

        // Handle refresh event
        val refreshView = findViewById<SwipeRefreshLayout>(R.id.home_pull_to_refresh)

        refreshView.setOnRefreshListener {
            CoroutineScope(Dispatchers.IO).launch {
                populateClassesList(teamsService, database, noClassesParent, false)

                refreshView.isRefreshing = false
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            // Show current user info
            val session = account.get()

            runOnUiThread {
                val pfp = findViewById<ImageView>(R.id.current_user_profile)

                findViewById<TextView>(R.id.current_user_name).text = session.name
                findViewById<TextView>(R.id.current_user_email).text = session.email

                Picasso.get().load("https://cloud.appwrite.io/v1/storage/buckets/646ef17593d213adfcf2/files/${session.id}/view?project=fryday").into(pfp)
            }

            // Get the list of the teams that the current user is in
            try {
                populateClassesList(teamsService, database, noClassesParent)
            } catch(e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@Home, e.toString(), Toast.LENGTH_LONG).show()
                }
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

    private suspend fun populateClassesList(teamsService: Teams, databases: Databases, noClassesParent: ConstraintLayout, showLoading: Boolean = true) {
        // Get the data
        val teams = teamsService.list().teams
        val userClasses = arrayListOf<ClassItem>()

        // Get the view
        val view = findViewById<RecyclerView>(R.id.home_classes_list)
        val bar = findViewById<ProgressBar>(R.id.home_classes_list_loading)

        runOnUiThread {
            noClassesParent.visibility = View.GONE
            view.visibility = View.GONE

            if(showLoading) {
                bar.visibility = View.VISIBLE
            }
        }

        // Iterate over each team
        for (team in teams) {
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
            if(teams.isEmpty()) {
                noClassesParent.visibility = View.VISIBLE
            }

            bar.visibility = View.GONE
            view.visibility = View.VISIBLE

            // Add the adapter
            view.adapter = ClassesListAdapter(userClasses, this@Home::openClassDashboard, this@Home)
            view.layoutManager = LinearLayoutManager(this@Home)
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