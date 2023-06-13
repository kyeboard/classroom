package me.kyeboard.classroom.utils

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity

fun startActivityWrapper(context: Context, classHandler: Class<out ComponentActivity>) {
    // Create a new intent
    val intent = Intent(context, classHandler)

    // Start the activity
    context.startActivity(intent)
}