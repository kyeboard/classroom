package me.kyeboard.classroom.screens

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.android.material.button.MaterialButton
import io.appwrite.services.Account
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.kyeboard.classroom.R
import me.kyeboard.classroom.screens.SubmissionView
import me.kyeboard.classroom.utils.get_appwrite_client
class Login : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Setup activity
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        actionBar?.hide()

        // Initialize appwrite client and req services
        val client = get_appwrite_client(this)
        val account = Account(client)

        // Handle get started button click
        findViewById<MaterialButton>(R.id.login).setOnClickListener {
            startGoogleOAuth(account)
        }
    }

    // Starts the home activity and ends the current activity
    private fun startHomeActivity() {
        // Create an intent
        val intent = Intent(this, SubmissionView::class.java)

        // Start the activity
        startActivity(intent)

        // End the current activity
        finish()
    }

    // Function that starts oauth activity
    private fun startGoogleOAuth(account: Account) {
        CoroutineScope(Dispatchers.IO).launch {
            // TODO: Add redirect to the website to prevent redirect attack
            try {
                // Create oauth session
                account.createOAuth2Session(this@Login, "google")

                // Send an successful toast message
                runOnUiThread {
                    Toast.makeText(this@Login, "Successfully logged in!", Toast.LENGTH_SHORT).show()
                }

                // Start home activity'''
                startHomeActivity()
            } catch(_: Exception) {
                runOnUiThread {
                    Toast.makeText(this@Login, "Login failed, try again!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}