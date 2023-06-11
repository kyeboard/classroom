package me.kyeboard.classroom.screens

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
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

        val viewPager = findViewById<ViewPager2>(R.id.assignment_view_pager)
        val tabLayout = findViewById<TabLayout>(R.id.assignment_view_tablayout)

        val assignment_id = intent.extras!!.getString("assignment_id")!!
        val class_id = intent.extras!!.getString("class_id")!!
        val accent_color = intent.extras!!.getString("accent_color")!!

        // Apply accent color
        window.statusBarColor = Color.parseColor(accent_color)
        findViewById<ConstraintLayout>(R.id.assignment_view_topbar).background.setTint(Color.parseColor(accent_color))

        val bundle = Bundle().apply {
            putString("assignment_id", assignment_id)
            putString("class_id", class_id)
            putString("accent_color", accent_color)
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
