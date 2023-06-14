package me.kyeboard.classroom.screens

import android.app.Activity
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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.WindowCompat
import io.appwrite.Client
import io.appwrite.Permission
import io.appwrite.Role
import io.appwrite.services.Databases
import io.appwrite.services.Teams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.kyeboard.classroom.R
import me.kyeboard.classroom.utils.animateTintWithBorder
import me.kyeboard.classroom.utils.animationColor
import me.kyeboard.classroom.utils.getAppwriteClient
import me.kyeboard.classroom.utils.visible

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

        // Remove status bar (make it transparent)
        WindowCompat.setDecorFitsSystemWindows(
            window,
            false
        )

        // Initiate variables
        var selectedColor = "#fee587"
        client = getAppwriteClient(applicationContext)
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

            loader.alpha = 0f
            visible(loader)
            loader.animate().alpha(1f).duration = 200

            // Create a team
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Create a new team for the user
                    val team = teams.create("unique()", className)

                    // Create the registry in the database
                    databases.createDocument(
                        "classes",
                        "registry",
                        team.id,
                        ClassItem(className, classSubject, selectedColor),
                        arrayListOf(
                            Permission.read(Role.team(team.id))
                        )
                    )

                    runOnUiThread {
                        // Set the result
                        setResult(Activity.RESULT_OK)

                        // End the current activity (the class is created)
                        finish()
                    }
                } catch(e: Exception) {
                    Log.e("new_class_create", e.message.toString())

                    runOnUiThread {
                        Toast.makeText(applicationContext, "Error while creating a class, try again!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun updateAccent(raw_color: String) {
        // Update topbar bg tint
        val color = Color.parseColor(raw_color)
        val black = Color.parseColor("#000000")

        val topbar = findViewById<ConstraintLayout>(R.id.topbar_parent)

        animationColor(topbar, color)

        // Update Create button bg tint
        animateTintWithBorder(findViewById<Button>(R.id.new_class_create), color, 200)

        // Reset selections
        colorsIndex.forEach { (k, v) ->
            val background = findViewById<View>(k).background.mutate() as GradientDrawable

            if(color == Color.parseColor(v)) {
                background.apply {
                    setColor(color)
                    setStroke(5, black)
                    cornerRadius = 10F
                }
            } else {
                background.apply {
                    setColor(Color.parseColor(v))
                    setStroke(0, black)
                    cornerRadius = 10F
                }
            }
        }
    }
}
