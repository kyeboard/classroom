package me.kyeboard.classroom

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import io.appwrite.services.Account
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.kyeboard.classroom.screens.Home
import me.kyeboard.classroom.screens.Login
import me.kyeboard.classroom.utils.get_appwrite_client
import me.kyeboard.classroom.utils.startActivityWrapper

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

            val packageClass = try {
                // Try to get the session
                 account.get()

                // Session exists... move on
                Home::class.java
            } catch(_: Exception) {
                 Login::class.java
            }

            startActivityWrapper(this@MainActivity, packageClass)
            finish()
        }
    }
}