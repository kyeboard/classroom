package me.kyeboard.classroom.screens

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import io.appwrite.extensions.tryJsonCast
import io.appwrite.services.Databases
import io.appwrite.services.Teams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.kyeboard.classroom.R
import me.kyeboard.classroom.adapters.ClassItem
import me.kyeboard.classroom.adapters.ClassesListAdapter
import me.kyeboard.classroom.utils.AppwriteService
import me.kyeboard.classroom.utils.AppwriteServiceSingleton
import me.kyeboard.classroom.utils.get_appwrite_client

class Home : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val appwriteService = AppwriteServiceSingleton.getInstance(this).get()!!
        val noClassesParent = findViewById<ConstraintLayout>(R.id.no_classes_found_parent)

        CoroutineScope(Dispatchers.IO).launch {
            val session = appwriteService.account.get()

            runOnUiThread {
                val pfp = findViewById<ImageView>(R.id.current_user_profile)

                findViewById<TextView>(R.id.current_user_name).text = session.name
                findViewById<TextView>(R.id.current_user_email).text = session.email

                Picasso.get().load("https://cloud.appwrite.io/v1/storage/buckets/646ef17593d213adfcf2/files/${session.id}/view?project=fryday").into(pfp)
            }

            // Get the list of the teams that the current user is in
            try {
                // Get the data
                val teams = appwriteService.teams.list().teams
                val userClasses = arrayListOf<ClassItem>()

                // Iterate over each team
                for (team in teams) {
                    userClasses.add(appwriteService.databases.getDocument("classes", "registery", team.id).data.tryJsonCast<ClassItem>()!!)
                }

                // Configure recycler view
                runOnUiThread {
                    val view = findViewById<RecyclerView>(R.id.home_classes_list)
                    findViewById<ProgressBar>(R.id.home_classes_list_loading).visibility = View.GONE

                    if(teams.isEmpty()) {
                        noClassesParent.visibility = View.VISIBLE
                    }

                    view.adapter = ClassesListAdapter(userClasses, this@Home::openClassDashboard, this@Home)
                    view.layoutManager = LinearLayoutManager(this@Home)
                }
            } catch(e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@Home, e.toString(), Toast.LENGTH_LONG).show()
                }
            }
        }

        findViewById<ImageButton>(R.id.open_new_class_popup).setOnClickListener {
            // Create a new intent
            val intent = Intent(this, NewClass::class.java)

            // Start the intent
            startActivity(intent)
        }
    }

    private fun openClassDashboard(id: String) {
        // Create a new intent for the dashboard
        val intent = Intent(this@Home, ClassDashboard::class.java)

        // Put the id
        intent.putExtra("class_id", id)

        // Start
        startActivity(intent)
    }
}