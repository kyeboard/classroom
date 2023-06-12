package me.kyeboard.classroom

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import io.appwrite.services.Account
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.kyeboard.classroom.screens.AssignmentView
import me.kyeboard.classroom.screens.Home
import me.kyeboard.classroom.screens.Login
import me.kyeboard.classroom.utils.get_appwrite_client
import kotlin.concurrent.thread

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition { true }

        WindowCompat.setDecorFitsSystemWindows(
            window,
            false
        )

        // Check if the session exists
        CoroutineScope(Dispatchers.IO).launch {
            val client = get_appwrite_client(applicationContext)
            val account = Account(client)

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