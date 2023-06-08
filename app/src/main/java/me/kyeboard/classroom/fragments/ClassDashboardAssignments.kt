package me.kyeboard.classroom.fragments

import AssignmentAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.appwrite.Query
import io.appwrite.services.Databases
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.kyeboard.classroom.R
import me.kyeboard.classroom.screens.AssignmentView
import me.kyeboard.classroom.utils.get_appwrite_client

class ClassDashboardAssignments : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_class_dashboard_assignments, container, false)

        val recyclerView = view.findViewById<RecyclerView>(R.id.classdashboard_assignments_recyclerview)
        val client = get_appwrite_client(view.context)
        val databases = Databases(client)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val data = databases.listDocuments("classes", "646f432ad59caafabf74", listOf(Query.orderAsc("due_date"))).documents
                val adapter = AssignmentAdapter(data, this@ClassDashboardAssignments::openAssignment)

                activity?.runOnUiThread {
                    recyclerView.adapter = adapter
                    recyclerView.layoutManager = LinearLayoutManager(view.context)
                }
            } catch(e: Exception) {
                Log.e("eee", e.message.toString())
            }
        }

        return view
    }

    private fun openAssignment(id: String) {
        val intent = Intent(this.context, AssignmentView::class.java)

        intent.putExtra("assignment_id", id)

        startActivity(intent)
    }
}