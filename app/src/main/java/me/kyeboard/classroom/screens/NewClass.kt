package me.kyeboard.classroom.screens

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.constraintlayout.widget.ConstraintLayout
import io.appwrite.services.Databases
import io.appwrite.services.Teams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.kyeboard.classroom.R
import me.kyeboard.classroom.utils.get_appwrite_client

data class ClassItem(val name: String, val subject: String, val color: String)

class NewClass : ComponentActivity() {
    private val colorsIndex = listOf(
        listOf(R.id.color_select_green, "#a3be8c"),
        listOf(R.id.color_select_yellow, "#fee587"),
        listOf(R.id.color_select_orange, "#d08770"),
        listOf(R.id.color_select_pink, "#b48ead")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_newclass)

        var selectedColor = "#fee587"

        updateAccent(selectedColor)

        for((selector, color) in colorsIndex) {
            findViewById<View>(selector as Int).setOnClickListener {
                selectedColor = color as String

                updateAccent(selectedColor)
            }
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
                database.createDocument("classes", "registery", team.id, ClassItem(className, classSubject, selectedColor))

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
        for((selector, respColor) in colorsIndex) {
            val background = findViewById<View>(selector as Int).background as GradientDrawable

            if(color == respColor as String) {
                background.apply {
                    setColor(Color.parseColor(color))
                    setStroke(5, Color.parseColor("#000000"))
                    cornerRadius = 10F
                }
            } else {
                background.apply {
                    setColor(Color.parseColor(respColor))
                    setStroke(0, Color.parseColor("#000000"))
                    cornerRadius = 10F
                }
            }
        }
    }
}
