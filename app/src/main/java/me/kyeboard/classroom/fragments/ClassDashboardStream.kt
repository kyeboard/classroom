package me.kyeboard.classroom.fragments

import Announcement
import AnnouncementAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
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
import me.kyeboard.classroom.utils.getAppwriteClient
import me.kyeboard.classroom.utils.invisible
import me.kyeboard.classroom.utils.visible

class ClassDashboardStream : Fragment() {
    private lateinit var client: Client
    private lateinit var databases: Databases
    private lateinit var accent_color: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_class_dashboard_stream, container, false)

        // Get items from bundle
        val arguments = requireArguments()
        val classId = arguments.getString("class_id")!!
        accent_color = arguments.getString("accent_color")!!

        // Get the view items
        val recyclerView = view.findViewById<RecyclerView>(R.id.class_dashboard_stream_announcements)
        val loading = view.findViewById<ConstraintLayout>(R.id.class_dashboard_stream_loading)
        val noAnnouncements = view.findViewById<ConstraintLayout>(R.id.no_announcements_parent)
        val swipeToRefresh = view.findViewById<SwipeRefreshLayout>(R.id.class_dashboard_stream_refresh)

        // Initiate appwrite services
        client = getAppwriteClient(view.context)
        databases = Databases(client)

        // Handle on swipe
        swipeToRefresh.setOnRefreshListener {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    updateStreamItems(classId, loading, noAnnouncements, recyclerView, view)

                    requireActivity().runOnUiThread {
                        swipeToRefresh.isRefreshing = false
                    }
                } catch(e: Exception) {
                    Log.e("update_stream_list", e.message.toString())

                    activity?.runOnUiThread {
                        Toast.makeText(activity?.applicationContext, "Error while fetching data, are you connected to internet?", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // Load data for the first time
        CoroutineScope(Dispatchers.IO).launch {
            updateStreamItems(classId, loading, noAnnouncements, recyclerView, view)
        }

        return view
    }

    private suspend fun updateStreamItems(classId: String, loading: ConstraintLayout, noAnnouncements: ConstraintLayout, recyclerView: RecyclerView, view: View) {
        // Get the list of the documents
        val data = databases.listDocuments(
            "classes",
            "announcements",
            arrayListOf(Query.orderDesc("\$createdAt"))
        ).documents

        // Create an array for announcements
        val announcements = arrayListOf<Announcement>()

        // Hide no announcements
        activity?.runOnUiThread {
            invisible(noAnnouncements)
        }

        // Iterate over the data
        for(i in data) {
            val casted = i.data.tryJsonCast<Announcement>()!!

            // Filter those announcements for this class
            if(casted.classid == classId) {
                announcements.add(casted)
            }
        }

        // Create adapter for announcements
        val adapter = AnnouncementAdapter(announcements, this@ClassDashboardStream::openAnnouncementView)

        // Set adapter and layout
        activity?.runOnUiThread {
            invisible(loading)

            if(announcements.isEmpty()) {
                visible(noAnnouncements)
            }

            recyclerView.adapter = adapter
            recyclerView.layoutManager = LinearLayoutManager(view.context)
        }
    }

    private fun openAnnouncementView(id: String) {
        // Create view intent
        val intent = Intent(this.context, AnnouncementView::class.java)

        // Add announcement id
        intent.putExtra("announcement_id", id)
        intent.putExtra("accent_color", accent_color)

        // Start
        startActivity(intent)
    }
}