package me.kyeboard.classroom.screens

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.WindowCompat
import io.appwrite.Client
import io.appwrite.services.Account
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.kyeboard.classroom.R
import me.kyeboard.classroom.utils.get_appwrite_client
import me.kyeboard.classroom.utils.startActivityWrapper

class Login : ComponentActivity() {
    private lateinit var client: Client
    private lateinit var account: Account

    override fun onCreate(savedInstanceState: Bundle?) {
        // Setup activity
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        window.statusBarColor = ResourcesCompat.getColor(resources, R.color.home_bg, theme)

        client = get_appwrite_client(applicationContext)
        account = Account(client)

        // Handle get started button click
        findViewById<Button>(R.id.login).setOnClickListener {
            startGoogleOAuth()
        }
    }

    // Starts the home activity and ends the current activity
    private fun startHomeActivity() {
        startActivityWrapper(this, Home::class.java)

        // End the current activity
        finish()
    }

    // Function that starts oauth activity
    private fun startGoogleOAuth() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Create oauth session
                account.createOAuth2Session(this@Login, "google")

                // Send an successful toast message
                runOnUiThread {
                    Toast.makeText(this@Login, "Successfully logged in!", Toast.LENGTH_SHORT).show()
                }

                // Start home activity
                startHomeActivity()
            } catch(_: Exception) {
                runOnUiThread {
                    Toast.makeText(this@Login, "Login failed, try again!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}