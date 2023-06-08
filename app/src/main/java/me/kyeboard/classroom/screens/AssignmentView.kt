package me.kyeboard.classroom.screens

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import me.kyeboard.classroom.R
import me.kyeboard.classroom.adapters.ViewPagerAdapter
import me.kyeboard.classroom.fragments.AssignmentViewSubmissions
import me.kyeboard.classroom.fragments.NewAssignmentTask

data class AssignmentItem(val title: String, val description: String, val attachments: ArrayList<String>, val author: String, val grade: Int, val due_date: String)

class AssignmentView : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assignmentview)

        window.statusBarColor = Color.parseColor("#fee587")

        val viewPager = findViewById<ViewPager2>(R.id.assignment_view_pager)
        val tabLayout = findViewById<TabLayout>(R.id.assignment_view_tablayout)

        //val assignment_id = intent.extras!!.getString("assignment_id")!!
        val assignment_id = "646f5e9c8ba221bf2df1"

        val newAssignmentTask = NewAssignmentTask().apply {
            arguments = Bundle().apply {
                putString("assignment_id", assignment_id)
            }
        }

        val adapter = ViewPagerAdapter(this)
        adapter.addFragment(newAssignmentTask)
        adapter.addFragment(AssignmentViewSubmissions())

        viewPager.adapter = adapter

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    viewPager.setCurrentItem(it.position, true)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // Do nothing
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // Do nothing
            }
        })
    }
}
