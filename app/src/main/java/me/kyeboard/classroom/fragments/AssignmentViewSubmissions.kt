package me.kyeboard.classroom.fragments

import SubmissionsAdapter
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.appwrite.services.Storage
import io.appwrite.services.Teams
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.kyeboard.classroom.R
import me.kyeboard.classroom.utils.get_appwrite_client

class AssignmentViewSubmissions : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_assignment_view_submissions, container, false)

        val client = get_appwrite_client(view.context)
        val storage = Storage(client)
        val teams = Teams(client)

        val recyclerView = view.findViewById<RecyclerView>(R.id.submissions_view_list)

        GlobalScope.launch {
            val memberships = teams.listMemberships("64720d2e62cdd9d39f04")
            val adapter = SubmissionsAdapter(memberships.memberships)

            activity?.runOnUiThread {
                recyclerView.adapter = adapter
                recyclerView.layoutManager = LinearLayoutManager(view.context)
            }
        }

        return view
    }
}