package me.kyeboard.classroom

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.kyeboard.classroom.screens.AnnouncementView
import me.kyeboard.classroom.screens.AssignmentView
import me.kyeboard.classroom.screens.ClassDashboard
import me.kyeboard.classroom.screens.Home
import me.kyeboard.classroom.screens.Login
import me.kyeboard.classroom.screens.NewAnnouncement
import me.kyeboard.classroom.screens.NewAssignment
import me.kyeboard.classroom.screens.NewClass
import me.kyeboard.classroom.screens.SubmissionView
import me.kyeboard.classroom.utils.AppwriteServiceSingleton

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition { true }

        // Get the singleton instance
        val service = AppwriteServiceSingleton.getInstance(this)

        // Check if the session exists
        CoroutineScope(Dispatchers.IO).launch {
            val nextIntent = try {
                service.get()!!.account.get()

                // Session exists... move on
                Intent(this@MainActivity, SubmissionView::class.java)
            } catch(_: Exception) {
                Intent(this@MainActivity, Login::class.java)
            }

            startActivity(nextIntent)
            finish()
        }
    }
}