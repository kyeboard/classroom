package me.kyeboard.classroom.fragments

import SubmissionsAdapter
import android.content.Intent
import android.os.Bundle
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
import me.kyeboard.classroom.screens.SubmissionView
import me.kyeboard.classroom.utils.getAppwriteClient

class AssignmentViewSubmissions : Fragment() {
    private lateinit var accent_color: String
    private lateinit var assignment_id: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_assignment_view_submissions, container, false)

        val client = getAppwriteClient(view.context)
        val storage = Storage(client)
        val teams = Teams(client)

        val recyclerView = view.findViewById<RecyclerView>(R.id.submissions_view_list)
        val class_id = requireArguments().getString("class_id")!!
        assignment_id = requireArguments().getString("assignment_id")!!
        accent_color = requireArguments().getString("accent_color")!!

        GlobalScope.launch {
            val memberships = teams.listMemberships(class_id)
            val adapter = SubmissionsAdapter(memberships.memberships, this@AssignmentViewSubmissions::openSubmissionView)

            activity?.runOnUiThread {
                recyclerView.adapter = adapter
                recyclerView.layoutManager = LinearLayoutManager(view.context)
            }
        }

        return view
    }

    public fun openSubmissionView(id: String) {
        val intent = Intent(this.context, SubmissionView::class.java)

        intent.putExtra("accent_color", accent_color)
        intent.putExtra("user_id", id)
        intent.putExtra("assignment_id", assignment_id)

        startActivity(intent)
    }
}