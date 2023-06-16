package me.kyeboard.classroom.fragments

import Announcement
import AnnouncementAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import io.appwrite.Client
import io.appwrite.Query
import io.appwrite.extensions.tryJsonCast
import io.appwrite.services.Databases
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.kyeboard.classroom.R
import me.kyeboard.classroom.screens.AnnouncementView
import me.kyeboard.classroom.screens.ClassDashboard
import me.kyeboard.classroom.utils.getAppwriteClient
import me.kyeboard.classroom.utils.invisible
import me.kyeboard.classroom.utils.visible

class ClassDashboardStream : Fragment() {
    private lateinit var client: Client
    private lateinit var databases: Databases
    private lateinit var accentColor: String
    private lateinit var classId: String
    private lateinit var loading: ConstraintLayout
    private lateinit var noAnnouncements: ConstraintLayout
    lateinit var recyclerView: RecyclerView
    private lateinit var view: View
    private lateinit var fetchCallback: () -> Unit

    companion object {
        fun newInstance(callback: () -> Unit): ClassDashboardStream {
            val instance = ClassDashboardStream()
            instance.fetchCallback = callback
            return instance
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_class_dashboard_stream, container, false)

        // Get items from bundle
        val arguments = requireArguments()
        classId = arguments.getString("class_id")!!
        accentColor = arguments.getString("accent_color")!!

        // Get the view items
        recyclerView = view.findViewById(R.id.class_dashboard_stream_announcements)
        loading = view.findViewById(R.id.class_dashboard_stream_loading)
        noAnnouncements = view.findViewById(R.id.no_announcements_parent)
        val swipeToRefresh = view.findViewById<SwipeRefreshLayout>(R.id.class_dashboard_stream_refresh)

        // Initiate appwrite services
        client = getAppwriteClient(view.context)
        databases = Databases(client)

        // Handle on swipe
        swipeToRefresh.setOnRefreshListener {
            updateStreamItems {
                swipeToRefresh.isRefreshing = false
            }
        }

        updateStreamItems {  }

        return view
    }

    fun updateStreamItems(callback: () -> Unit = { }) {
        invisible(noAnnouncements)
        invisible(recyclerView)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Get the list of the documents
                val announcements = databases.listDocuments(
                    "classes",
                    "announcements",
                    arrayListOf(Query.orderDesc("\$createdAt"))
                ).documents
                    .map {
                        it.data.tryJsonCast<Announcement>()!!
                    }
                    .filter {
                        it.classid == classId
                    }

                // Create adapter for announcements
                val adapter = AnnouncementAdapter(
                    announcements,
                    this@ClassDashboardStream::openAnnouncementView
                )

                // Set adapter and layout
                activity?.runOnUiThread {
                    invisible(loading)

                    if (announcements.isEmpty()) {
                        visible(noAnnouncements)
                    }

                    fetchCallback()

                    recyclerView.adapter = adapter
                    recyclerView.layoutManager = LinearLayoutManager(view.context)

                    callback()
                }
            } catch(e: Exception) {
                Log.e("update_stream_list", e.message.toString())

                Toast.makeText(view.context, "Cannot fetch stream, are you connected to internet?", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openAnnouncementView(id: String) {
        // Create view intent
        val intent = Intent(this.context, AnnouncementView::class.java)

        // Add announcement id
        intent.putExtra("announcement_id", id)
        intent.putExtra("accent_color", accentColor)

        // Start
        startActivity(intent)
    }
}