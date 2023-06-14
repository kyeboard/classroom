package me.kyeboard.classroom.screens

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.WindowCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import io.appwrite.Query
import io.appwrite.services.Account
import io.appwrite.services.Teams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.kyeboard.classroom.R
import me.kyeboard.classroom.adapters.ViewPagerAdapter
import me.kyeboard.classroom.fragments.AssignmentViewSubmissions
import me.kyeboard.classroom.fragments.NewAssignmentTask
import me.kyeboard.classroom.utils.getAppwriteClient
import me.kyeboard.classroom.utils.invisible

data class AssignmentItem(val title: String, val description: String, val attachments: ArrayList<String>, val author: String, val grade: Int, val due_date: String)

class AssignmentView : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assignmentview)

        val viewPager = findViewById<ViewPager2>(R.id.assignment_view_pager)
        val tabLayout = findViewById<TabLayout>(R.id.assignment_view_tablayout)

        val assignmentId = intent.extras!!.getString("assignment_id")!!
        val classId = intent.extras!!.getString("class_id")!!
        val accentColor = intent.extras!!.getString("accent_color")!!

        val client = getAppwriteClient(applicationContext)
        val account = Account(client)
        val teams = Teams(client)

        window.statusBarColor = Color.parseColor(accentColor)

        // Apply accent color
        findViewById<ConstraintLayout>(R.id.assignment_view_topbar).background.mutate().setTint(Color.parseColor(accentColor))

        // Create bundle
        val bundle = Bundle().apply {
            putString("assignment_id", assignmentId)
            putString("class_id", classId)
            putString("accent_color", accentColor)
        }

        // Create adapters
        val newAssignmentTask = NewAssignmentTask().apply {
            arguments = bundle
        }
        val newAssignmentSubmissions = AssignmentViewSubmissions().apply {
            arguments = bundle
        }

        val adapter = ViewPagerAdapter(this)
        adapter.addFragment(newAssignmentTask)
        adapter.addFragment(newAssignmentSubmissions)

        viewPager.adapter = adapter

        CoroutineScope(Dispatchers.IO).launch {
            val session = account.get()
            val roles = teams.listMemberships(classId, arrayListOf(Query.equal("userId", session.id))).memberships[0].roles

            if(!roles.contains("owner")) {
                runOnUiThread {
                    invisible(tabLayout)
                }
            }
        }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    viewPager.setCurrentItem(it.position, true)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }
}
