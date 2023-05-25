package me.kyeboard.oxide.screens

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.appwrite.extensions.tryJsonCast
import io.appwrite.services.Databases
import io.appwrite.services.Teams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.kyeboard.oxide.R
import me.kyeboard.oxide.adapters.ClassItem
import me.kyeboard.oxide.adapters.ClassesListAdapter
import me.kyeboard.oxide.utils.get_appwrite_client

class Home : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val client = get_appwrite_client(this)
        val teams = Teams(client)
        val database = Databases(client)

        CoroutineScope(Dispatchers.IO).launch {
            // Get the list of the teams that the current user is in
            try {
                val teams = teams.list()
                val user_classes = arrayListOf<ClassItem>()

                // Iterate over each team
                for (team in teams.teams) {
                    user_classes.add(database.getDocument("classes", "registery", team.id).data.tryJsonCast<ClassItem>()!!)
                }

                // Configure recycler view
                runOnUiThread {
                    val view = findViewById<RecyclerView>(R.id.home_classes_list)

                    view.adapter = ClassesListAdapter(user_classes, this@Home::openClassDashboard)
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

    private fun openClassDashboard(id: String): Unit {
        val intent = Intent(this@Home, ClassDashboard::class.java)
        startActivity(intent)
    }
}