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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import io.appwrite.Query
import io.appwrite.extensions.tryJsonCast
import io.appwrite.services.Databases
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.kyeboard.classroom.R
import me.kyeboard.classroom.screens.AnnouncementView
import me.kyeboard.classroom.utils.get_appwrite_client

class ClassDashboardStream : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_class_dashboard_stream, container, false)

        // Get the view items
        val recyclerView = view.findViewById<RecyclerView>(R.id.class_dashboard_stream_announcements)
        val classId = requireArguments().getString("class_id")!!
        val loading = view.findViewById<ConstraintLayout>(R.id.class_dashboard_stream_loading)
        val noAnnouncements = view.findViewById<ConstraintLayout>(R.id.no_announcements_parent)

        // Initiate appwrite services
        val client = get_appwrite_client(view.context)
        val databases = Databases(client)

        val swipeToRefresh = view.findViewById<SwipeRefreshLayout>(R.id.class_dashboard_stream_refresh)

        swipeToRefresh.setOnRefreshListener {
            CoroutineScope(Dispatchers.IO).launch {
                updateStreamItems(databases, classId, loading, noAnnouncements, recyclerView, view)

                requireActivity().runOnUiThread {
                    swipeToRefresh.isRefreshing = false
                }
            }
        }

        // Load data
        CoroutineScope(Dispatchers.IO).launch {
            updateStreamItems(databases, classId, loading, noAnnouncements, recyclerView, view)
        }

        return view
    }

    private suspend fun updateStreamItems(databases: Databases, classId: String, loading: ConstraintLayout, noAnnouncements: ConstraintLayout, recyclerView: RecyclerView, view: View) {
        val data = databases.listDocuments("classes", "647c1b704310bb8f0fed", arrayListOf(Query.orderDesc("\$createdAt"))).documents
        val announcements = arrayListOf<Announcement>()

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
            loading.visibility = View.GONE

            if(announcements.isEmpty()) {
                noAnnouncements.visibility = View.VISIBLE
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

        // Start
        startActivity(intent)
    }
}