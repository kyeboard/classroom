package me.kyeboard.classroom.utils

import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.appwrite.services.Account
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.kyeboard.classroom.screens.Login

fun loadUserSession(view: AppCompatActivity, account: Account, name_container: Int, email_container: Int, pfpContainer: Int) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val session = account.get()

            view.runOnUiThread {
                setText(view, name_container, session.name)
                setText(view, email_container, session.email)

                imageInto(
                    view,
                    "https://cloud.appwrite.io/v1/storage/buckets/userpfps/files/${session.id}/view?project=classroom",
                    pfpContainer
                )
            }
        } catch(e: Exception) {
            // User has some issue with session so its better for a login
            startActivityWrapper(view, Login::class.java)

            // Finish current since there is no more usage of it
            view.finish()
        }
    }
}

fun logoutAndRedirect(account: Account, activity: AppCompatActivity) {
    CoroutineScope(Dispatchers.IO).launch {
        // Delete current session
        account.deleteSession("current")

        // Send toast
        activity.runOnUiThread {
            Toast.makeText(activity, "Successfully logged out!", Toast.LENGTH_SHORT).show()

            val intent = Intent(activity.applicationContext, Login::class.java)
            activity.startActivity(intent)
        }

        // Redirect to login activity
        activity.finish()
    }
}