package me.kyeboard.classroom

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import io.appwrite.services.Account
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.kyeboard.classroom.screens.AssignmentView
import me.kyeboard.classroom.screens.Home
import me.kyeboard.classroom.screens.Login
import me.kyeboard.classroom.utils.get_appwrite_client

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        val client = get_appwrite_client(this)
        val account = Account(client)

        splashScreen.setKeepOnScreenCondition { true }

        // Check if the session exists
        CoroutineScope(Dispatchers.IO).launch {
            val nextIntent = try {
                // Try to get the session
                account.get()

                // Session exists... move on
                Intent(this@MainActivity, Home::class.java)
            } catch(_: Exception) {
                Intent(this@MainActivity, Login::class.java)
            }

            startActivity(nextIntent)
            finish()
        }
    }
}