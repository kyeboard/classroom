package me.kyeboard.classroom.fragments

import SubmissionsAdapter
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.appwrite.services.Databases
import io.appwrite.services.Teams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.kyeboard.classroom.R
import me.kyeboard.classroom.screens.SubmissionView
import me.kyeboard.classroom.utils.getAppwriteClient
import me.kyeboard.classroom.utils.invisible
import java.math.BigInteger
import java.security.MessageDigest

fun hash_md5(target: String): String {
    return BigInteger(1, MessageDigest.getInstance("MD5").digest(target.toByteArray())).toString(16).padStart(32, '0')
}

class AssignmentViewSubmissions : Fragment() {
    private lateinit var accentColor: String
    private lateinit var assignmentId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate view
        val view = inflater.inflate(R.layout.fragment_assignment_view_submissions, container, false)

        // Initialze appwrite service
        val client = getAppwriteClient(view.context)
        val teams = Teams(client)
        val databases = Databases(client)

        // View holders
        val recyclerView = view.findViewById<RecyclerView>(R.id.submissions_view_list)
        val progressBar = view.findViewById<ProgressBar>(R.id.progress_bar)

        // Get from arguments
        val classId = requireArguments().getString("class_id")!!
        assignmentId = requireArguments().getString("assignment_id")!!
        accentColor = requireArguments().getString("accent_color")!!

        // Load students
        CoroutineScope(Dispatchers.IO).launch {
            val memberships = teams.listMemberships(classId)
            val submission_list: ArrayList<Boolean> = arrayListOf()

            for(i in memberships.memberships) {
                try {
                    databases.getDocument(
                        "classes",
                        "submissions",
                        hash_md5("$assignmentId-${i.userId}")
                    )

                    submission_list.add(true)
                } catch(e: Exception) {
                    submission_list.add(false)
                }
            }

            val adapter = SubmissionsAdapter(memberships.memberships, submission_list, this@AssignmentViewSubmissions::openSubmissionView)

            activity?.runOnUiThread {
                recyclerView.adapter = adapter
                recyclerView.layoutManager = LinearLayoutManager(view.context)

                invisible(progressBar)
            }
        }

        return view
    }

    private fun openSubmissionView(id: String) {
        val intent = Intent(this.context, SubmissionView::class.java)

        intent.putExtra("accent_color", accentColor)
        intent.putExtra("user_id", id)
        intent.putExtra("assignment_id", assignmentId)

        startActivity(intent)
    }
}