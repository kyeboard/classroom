package me.kyeboard.classroom.utils

import android.content.Context
import io.appwrite.Client

fun get_appwrite_client(context: Context): Client {
    return Client(context)
        .setEndpoint("https://cloud.appwrite.io/v1")
        .setProject("fryday")
}