package me.kyeboard.classroom.utils

import android.content.Context
import io.appwrite.Client

fun getAppwriteClient(context: Context): Client {
    return Client(context)
        .setEndpoint("https://cloud.appwrite.io/v1")
        .setProject("fryday")
}
