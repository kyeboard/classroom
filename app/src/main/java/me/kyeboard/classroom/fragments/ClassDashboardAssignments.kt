package me.kyeboard.classroom.fragments

import Assignment
import AssignmentAdapter
import android.app.DatePickerDialog
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
import me.kyeboard.classroom.utils.getAppwriteClient
import me.kyeboard.classroom.utils.invisible
import me.kyeboard.classroom.utils.visible
import java.util.Calendar
import java.util.Date

class ClassDashboardAssignments : Fragment() {
    private lateinit var client: Client
    private lateinit var databases: Databases
    private lateinit var accentColor: String
    private lateinit var classId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_class_dashboard_assignments, container, false)

        // View holders
        val recyclerView = view.findViewById<RecyclerView>(R.id.classdashboard_assignments_recyclerview)

        // Appwrite services
        client = getAppwriteClient(view.context)
        databases = Databases(client)

        // Get items from bundle
        classId = requireArguments().getString("class_id")!!
        accentColor = requireArguments().getString("accent_color")!!

        // Get today's date
        val today = Date()

        // Filters
        val missedFilter: (Date, Boolean) -> Boolean = { date, _ -> date.before(today) }
        val assignedFilter: (Date, Boolean) -> Boolean = { date, _ -> date.after(today) || date == today }
        val submittedFilter: (Date, Boolean) -> Boolean = { _, has_submitted -> has_submitted }

        // Initial
        CoroutineScope(Dispatchers.IO).launch {
            populateAssignments(recyclerView, view, assignedFilter, classId)
        }

        // Handle tab layout chances
        view.findViewById<TabLayout>(R.id.assignments_tablayout).addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val filter = when(tab!!.position) {
                    0 -> missedFilter
                    1 -> assignedFilter
                    else -> submittedFilter
                }

                CoroutineScope(Dispatchers.IO).launch {
                    populateAssignments(recyclerView, view, filter, classId)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        return view
    }

    private suspend fun populateAssignments(recyclerView: RecyclerView, view: View, filter: (Date, Boolean) -> Boolean, class_id: String) {
        val loading = view.findViewById<ProgressBar>(R.id.class_dashboard_assignments_loading)
        val noItemsFound = view.findViewById<ConstraintLayout>(R.id.no_assignments_parent)

        try {
            activity?.runOnUiThread {
                visible(loading)
                invisible(recyclerView)
                invisible(noItemsFound)
            }

            val data = databases.listDocuments(
                "classes",
                "assignments",
                listOf(Query.orderAsc("due_date"))
            ).documents
            val parsedData = arrayListOf<Assignment>()

            // Filter data here
            for(item in data) {
                val parsed = item.data.tryJsonCast<Assignment>()!!

                if(filter(parsed.due_date, true) && parsed.classid == class_id) {
                    parsedData.add(parsed)
                }
            }

            // Adapter initialization
            val adapter = AssignmentAdapter(parsedData, this@ClassDashboardAssignments::openAssignment)

            activity?.runOnUiThread {
                invisible( loading)
                visible(recyclerView)

                // Set adapter
                recyclerView.adapter = adapter
                recyclerView.layoutManager = LinearLayoutManager(view.context)

                if(parsedData.isEmpty()) {
                    visible(noItemsFound)
                }
            }
        } catch(e: Exception) {
            Log.e("load_assignments_error", e.message.toString())
        }
    }

    private fun openAssignment(id: String) {
        val intent = Intent(this.context, AssignmentView::class.java)

        intent.putExtra("assignment_id", id)
        intent.putExtra("accent_color", accentColor)
        intent.putExtra("class_id", classId)

        startActivity(intent)
    }
}