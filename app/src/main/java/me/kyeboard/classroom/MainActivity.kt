package me.kyeboard.classroom

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.android.material.button.MaterialButton
import io.appwrite.services.Account
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.kyeboard.classroom.screens.Home
import me.kyeboard.classroom.utils.get_appwrite_client

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val client = get_appwrite_client(this)
        val account = Account(client)

        // Check if the session exists
        CoroutineScope(Dispatchers.IO).launch {
            // Check if the session exists
            try {
                // Try to get the session
                account.get()

                // If exists, redirect to the home page
                startHomeActivity()
            } catch(_: Exception) {

            }
        }

        findViewById<MaterialButton>(R.id.login).setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                // TODO: Add redirect to the website to prevent redirect attack
                try {
                    // Create oauth session
                    account.createOAuth2Session(this@MainActivity, "google")

                    // Send an successful toast message
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "Successfully logged in!", Toast.LENGTH_SHORT).show()
                    }

                    // Start home activity'''
                    startHomeActivity()
                } catch(_: Exception) {
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "Login failed, try again!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    // Starts the home activity and ends the current activity
    private fun startHomeActivity() {
        // Create an intent
        val intent = Intent(this, Home::class.java)

        // Start the activity
        startActivity(intent)

        // End the current activity
        finish()
    }
}