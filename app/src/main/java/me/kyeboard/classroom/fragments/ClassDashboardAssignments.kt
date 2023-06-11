package me.kyeboard.classroom.fragments

import Assignment
import AssignmentAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import io.appwrite.Client
import io.appwrite.Query
import io.appwrite.extensions.tryJsonCast
import io.appwrite.services.Databases
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.kyeboard.classroom.R
import me.kyeboard.classroom.screens.AssignmentView
import me.kyeboard.classroom.utils.get_appwrite_client
import java.time.LocalDate
import java.util.Date

class ClassDashboardAssignments : Fragment() {
    private lateinit var client: Client
    private lateinit var databases: Databases

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_class_dashboard_assignments, container, false)

        val recyclerView = view.findViewById<RecyclerView>(R.id.classdashboard_assignments_recyclerview)
        client = get_appwrite_client(view.context)
        databases = Databases(client)

        val today = Date()

        // Filters
        val missed_filter: (Date, Boolean) -> Boolean = { date, _ -> date.before(today) }
        val assigned_filter: (Date, Boolean) -> Boolean = { date, _ -> date == today }
        val submitted_filter: (Date, Boolean) -> Boolean = { _, has_submitted -> has_submitted }

        CoroutineScope(Dispatchers.IO).launch {
            populateAssignments(recyclerView, view, assigned_filter)
        }

        // Handle tab layout chances
        view.findViewById<TabLayout>(R.id.assignments_tablayout).addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val filter = when(tab!!.position) {
                    0 -> missed_filter
                    1 -> assigned_filter
                    else -> submitted_filter
                }

                Log.d("tt", tab!!.position.toString())

                CoroutineScope(Dispatchers.IO).launch {
                    populateAssignments(recyclerView, view, filter)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        return view
    }

    private suspend fun populateAssignments(recyclerView: RecyclerView, view: View, filter: (Date, Boolean) -> Boolean) {
        val loading = view.findViewById<ProgressBar>(R.id.class_dashboard_assignments_loading)
        val no_items_found = view.findViewById<ConstraintLayout>(R.id.no_assignments_parent)

        try {
            activity?.runOnUiThread {
                loading.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            }

            val data = databases.listDocuments("classes", "646f432ad59caafabf74", listOf(Query.orderAsc("due_date"))).documents
            val parsed_data = arrayListOf<Assignment>()

            for(item in data) {
                val parsed = item.data.tryJsonCast<Assignment>()!!

                if(filter(parsed.due_date, true)) {
                    parsed_data.add(parsed)
                }
            }

            val adapter = AssignmentAdapter(parsed_data, this@ClassDashboardAssignments::openAssignment)

            activity?.runOnUiThread {
                loading.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                recyclerView.adapter = adapter
                recyclerView.layoutManager = LinearLayoutManager(view.context)
            }
        } catch(e: Exception) {
            Log.e("eee", e.message.toString())
        }
    }

    private fun openAssignment(id: String) {
        val intent = Intent(this.context, AssignmentView::class.java)

        intent.putExtra("assignment_id", id)

        startActivity(intent)
    }
}