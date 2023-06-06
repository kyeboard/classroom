package me.kyeboard.classroom.screens

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import com.squareup.picasso.Picasso
import io.appwrite.services.Databases
import io.appwrite.services.Teams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.kyeboard.classroom.R
import me.kyeboard.classroom.utils.get_appwrite_client

data class ClassItem(val name: String, val subject: String, val color: String)

class NewClass : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_newclass)

        var color = "#fee587"

        updateAccent(color)

        findViewById<View>(R.id.color_select_green).setOnClickListener {
            color = "#a3be8c"
            updateAccent(color)
        }

        findViewById<View>(R.id.color_select_orange).setOnClickListener {
            color = "#d08770"
            updateAccent(color)
        }

        findViewById<View>(R.id.color_select_pink).setOnClickListener {
            color = "#b48ead"
            updateAccent(color)
        }

        findViewById<View>(R.id.color_select_yellow).setOnClickListener {
            color = "#fee587"
            updateAccent(color)
        }

        // Bind buttons that closes the self instance
        findViewById<ImageView>(R.id.newclass_destory_self).setOnClickListener {
            finish()
        }

        // Default items
        val client = get_appwrite_client(this)
        val teams = Teams(client)
        val database = Databases(client)

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
                database.createDocument("classes", "registery", team.id, ClassItem(className, classSubject, color))

                // End the current activity (the class is created)
                finish()
            }
        }
    }

    private fun updateAccent(color: String) {
        window.statusBarColor = Color.parseColor(color)

        findViewById<ConstraintLayout>(R.id.topbar_parent).background.setTint(Color.parseColor(color))
        val buttonBackground = findViewById<Button>(R.id.new_class_create).background as GradientDrawable

        buttonBackground.apply {
            setColor(Color.parseColor(color))
            setStroke(5, Color.parseColor("#000000"))
            cornerRadius = 10F
        }

        // Reset selections
        for(i in listOf(
            listOf(R.id.color_select_green, "#a3be8c"),
            listOf(R.id.color_select_yellow, "#fee587"),
            listOf(R.id.color_select_orange, "#d08770"),
            listOf(R.id.color_select_pink, "#b48ead")
        )) {
            val background = findViewById<View>(i[0] as Int).background as GradientDrawable

            if(color == i[1] as String) {
                background.apply {
                    setColor(Color.parseColor(color))
                    setStroke(5, Color.parseColor("#000000"))
                    cornerRadius = 10F
                }
            } else {
                background.apply {
                    setColor(Color.parseColor(i[1] as String))
                    setStroke(0, Color.parseColor("#000000"))
                    cornerRadius = 10F
                }
            }
        }
    }

    /*
    private fun updateHeaderImage(url: String) {
        Picasso.get().load(url).into(findViewById<ImageView>(R.id.selected_header_preview))
    } */
}
