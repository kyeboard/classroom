package me.kyeboard.classroom.utils

import android.content.Context
import io.appwrite.Client
import io.appwrite.services.Account
import io.appwrite.services.Databases
import io.appwrite.services.Teams
import java.lang.ref.WeakReference

fun get_appwrite_client(context: Context): Client {
    return Client(context)
        .setEndpoint("https://cloud.appwrite.io/v1")
        .setProject("fryday")
}

class AppwriteService(context: Context) {
    val client: Client = Client(context)
        .setEndpoint("https://cloud.appwrite.io/v1")
        .setProject("fryday")
    val account = Account(client)
    val teams = Teams(client)
    val databases = Databases(client)
}

object AppwriteServiceSingleton {
    private var instance: WeakReference<AppwriteService>? = null

    fun getInstance(context: Context): WeakReference<AppwriteService> {
        if(instance == null) {
            instance = WeakReference(AppwriteService(context))
        }

        return instance!!
    }
}