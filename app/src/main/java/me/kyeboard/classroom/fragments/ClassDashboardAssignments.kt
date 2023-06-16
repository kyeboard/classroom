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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
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
    private lateinit var recyclerView: RecyclerView
    private lateinit var class_id: String
    private lateinit var view: View
    private val today = Date()
    val missedFilter: (Date) -> Boolean = { date -> date.before(today) }
    val assignedFilter: (Date) -> Boolean = { date -> date.after(today) || date == today }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_class_dashboard_assignments, container, false)

        // View holders
        recyclerView = view.findViewById(R.id.classdashboard_assignments_recyclerview)

        // Appwrite services
        client = getAppwriteClient(view.context)
        databases = Databases(client)

        // Get items from bundle
        class_id = requireArguments().getString("class_id")!!
        accentColor = requireArguments().getString("accent_color")!!

        // Initial
        populateAssignments(missedFilter)

        val refreshLayout = view.findViewById<SwipeRefreshLayout>(R.id.assignments_refresh_layout)
        val tablayout = view.findViewById<TabLayout>(R.id.assignments_tablayout)

        refreshLayout.setOnRefreshListener {
            val filter = when(tablayout.selectedTabPosition) {
                0 -> missedFilter
                else -> assignedFilter
            }

            populateAssignments(filter) {
                refreshLayout.isRefreshing = false
            }
        }

        // Handle tab layout chances
        tablayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val filter = when(tab!!.position) {
                    0 -> missedFilter
                    else -> assignedFilter
                }

                populateAssignments(filter)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        return view
    }

    fun populateAssignments(filter: (Date) -> Boolean, callback: () -> Unit = { }) {
        val loading = view.findViewById<ProgressBar>(R.id.class_dashboard_assignments_loading)
        val noItemsFound = view.findViewById<ConstraintLayout>(R.id.no_assignments_parent)

        visible(loading)
        invisible(recyclerView)
        invisible(noItemsFound)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val data = databases.listDocuments("classes","assignments", listOf(Query.orderAsc("due_date")))
                    .documents
                    .map { it.data.tryJsonCast<Assignment>()!! }
                    .filter {
                        Log.d("tt", it.toString())
                        filter(it.due_date) && it.classid == class_id
                    }

                // Adapter initialization
                val adapter = AssignmentAdapter(data, this@ClassDashboardAssignments::openAssignment)

                activity?.runOnUiThread {
                    invisible(loading)
                    visible(recyclerView)

                    // Set adapter
                    recyclerView.adapter = adapter
                    recyclerView.layoutManager = LinearLayoutManager(view.context)

                    if(data.isEmpty()) {
                        visible(noItemsFound)
                    }

                    callback()
                }
            } catch(e: Exception) {
                Log.e("load_assignments_error", e.message.toString())
            }
        }
    }

    private fun openAssignment(id: String) {
        val intent = Intent(context, AssignmentView::class.java)

        intent.putExtra("assignment_id", id)
        intent.putExtra("accent_color", accentColor)
        intent.putExtra("class_id", classId)

        startActivity(intent)
    }
}