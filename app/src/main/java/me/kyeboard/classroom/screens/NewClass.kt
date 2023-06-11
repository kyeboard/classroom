package me.kyeboard.classroom.screens

import android.app.Activity
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
import io.appwrite.Client
import io.appwrite.services.Databases
import io.appwrite.services.Teams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.kyeboard.classroom.R
import me.kyeboard.classroom.utils.get_appwrite_client

data class ClassItem(val name: String, val subject: String, val color: String)

class NewClass : ComponentActivity() {
    private val colorsIndex: Map<Int, String> = mapOf(
        R.id.color_select_green to "#a3be8c",
        R.id.color_select_yellow to "#fee587",
        R.id.color_select_orange to "#d08770",
        R.id.color_select_pink to "#b48ead"
    )

    private lateinit var client: Client
    private lateinit var teams: Teams
    private lateinit var databases: Databases

    override fun onCreate(savedInstanceState: Bundle?) {
        // Initiate view
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_newclass)

        // Initiate variables
        var selectedColor = "#fee587"
        client = get_appwrite_client(applicationContext)
        teams = Teams(client)
        databases = Databases(client)

        // Initiate view items
        val loader = findViewById<ConstraintLayout>(R.id.new_class_loading)

        // Update accept for the first time
        updateAccent(selectedColor)

        // Add listener to all the elements
        colorsIndex.forEach { (k, v) ->
            findViewById<View>(k).setOnClickListener {
                selectedColor = v
                updateAccent(selectedColor)
            }
        }

        // Bind buttons that closes the self instance
        findViewById<ImageView>(R.id.newclass_destory_self).setOnClickListener {
            finish()
        }

        // Handle new class create button clicks
        findViewById<Button>(R.id.new_class_create).setOnClickListener {
            // Get the values entered by the user
            val className = findViewById<EditText>(R.id.new_class_name).text.toString()
            val classSubject = findViewById<EditText>(R.id.new_class_subject).text.toString()

            // Make sure they are not blank
            if (className.isBlank() || classSubject.isBlank()) {
                Toast.makeText(applicationContext, "Fill in all details before continuing", Toast.LENGTH_LONG).show()

                return@setOnClickListener
            }

            window.statusBarColor = Color.parseColor("#DD2E3440")
            loader.visibility = View.VISIBLE

            // Create a team
            CoroutineScope(Dispatchers.IO).launch {
                // Create a new team for the user
                val team = teams.create("unique()", className)

                // Create the registry in the database
                // TODO: Fix the registry spelling
                databases.createDocument("classes", "registery", team.id, ClassItem(className, classSubject, selectedColor))

                runOnUiThread {
                    // Set the result
                    setResult(Activity.RESULT_OK)

                    // End the current activity (the class is created)
                    finish()
                }
            }
        }
    }

    private fun updateAccent(color: String) {
        // Update status bar color
        window.statusBarColor = Color.parseColor(color)

        // Update topbar bg tint
        findViewById<ConstraintLayout>(R.id.topbar_parent).background.setTint(Color.parseColor(color))

        // Update Create button bg tint
        val buttonBackground = findViewById<Button>(R.id.new_class_create).background as GradientDrawable

        buttonBackground.apply {
            setColor(Color.parseColor(color))
            setStroke(5, Color.parseColor("#000000"))
            cornerRadius = 10F
        }

        // Reset selections
        colorsIndex.forEach { (k, v) ->
            val background = findViewById<View>(k).background as GradientDrawable

            if(color == v) {
                background.apply {
                    setColor(Color.parseColor(color))
                    setStroke(5, Color.parseColor("#000000"))
                    cornerRadius = 10F
                }
            } else {
                background.apply {
                    setColor(Color.parseColor(v))
                    setStroke(0, Color.parseColor("#000000"))
                    cornerRadius = 10F
                }
            }
        }
    }
}
