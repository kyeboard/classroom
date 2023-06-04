package me.kyeboard.classroom.screens

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import com.squareup.picasso.Picasso
import io.appwrite.services.Databases
import io.appwrite.services.Teams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.kyeboard.classroom.R
import me.kyeboard.classroom.utils.get_appwrite_client

data class ClassItem(val name: String, val header: String, val subject: String)

class NewClass : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_newclass)

        // Bind buttons that closes the self instance
        findViewById<ImageView>(R.id.newclass_destory_self).setOnClickListener {
            finish()
        }
        findViewById<Button>(R.id.newclass_cancel_self).setOnClickListener {
            finish()
        }

        // Default items
        var headerImage = "https://cloud.appwrite.io/v1/storage/buckets/646460e48963e000edd6/files/landscape1/view?project=fryday"
        val client = get_appwrite_client(this)
        val teams = Teams(client)
        val database = Databases(client)

        // Initial
        updateHeaderImage(headerImage)

        // Activity handler which returns the newly selected header
        val headerHandler = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            try {
                headerImage = result.data?.data!!.toString()
                updateHeaderImage(headerImage)
            } catch(_: Exception) {

            }
        }

        // Handle new class create button clicks
        findViewById<Button>(R.id.new_class_create).setOnClickListener {
            // Get the values entered by the user
            val className = findViewById<EditText>(R.id.new_class_name).text.toString()
            val classSubject = findViewById<EditText>(R.id.new_class_subject).text.toString()

            // Make sure they are not blank
            if (className.isBlank() || classSubject.isBlank()) {
                Toast.makeText(this, "Fill in all details before continuing", Toast.LENGTH_LONG).show()

                return@setOnClickListener
            }

            // Create a team
            CoroutineScope(Dispatchers.IO).launch {
                // Create a new team for the user
                val team = teams.create("unique()", className)

                // Create the registry in the database
                // TODO: Fix the registry spelling
                database.createDocument("classes", "registery", team.id, ClassItem(className, headerImage, classSubject))

                // End the current activity (the class is created)
                finish()
            }
        }

        // Launch select header activity to select a header
        findViewById<CardView>(R.id.select_header).setOnClickListener {
            // Create intent for that activity
            val intent = Intent(this, SelectHeader::class.java)

            // Launch with the result handler
            headerHandler.launch(intent)
        }
    }

    private fun updateHeaderImage(url: String) {
        Picasso.get().load(url).into(findViewById<ImageView>(R.id.selected_header_preview))
    }
}
