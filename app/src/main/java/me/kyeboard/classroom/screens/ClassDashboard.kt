package me.kyeboard.classroom.screens

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import me.kyeboard.classroom.R
import me.kyeboard.classroom.adapters.ViewPagerAdapter
import me.kyeboard.classroom.fragments.ClassDashboardAssignments
import me.kyeboard.classroom.fragments.ClassDashboardClasses
import me.kyeboard.classroom.fragments.ClassDashboardStream

class ClassDashboard : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_classdashboard)

        val class_id = intent.extras!!.getString("class_id")

        findViewById<ImageButton>(R.id.class_dashboard_new_announcement).setOnClickListener {
            val intent = Intent(this, NewAnnouncement::class.java)
            intent.putExtra("class_id", class_id)
            startActivity(intent)
        }

        val viewPager = findViewById<ViewPager2>(R.id.classdashbord_viewpager)
        val tabLayout = findViewById<TabLayout>(R.id.class_dashboard_tablayout)

        val bundle = Bundle().apply {
            putString("class_id", class_id)
        }
        val classDashboardStream = ClassDashboardStream().apply {
            arguments = bundle
        }

        val adapter = ViewPagerAdapter(this)
        adapter.addFragment(classDashboardStream)
        adapter.addFragment(ClassDashboardAssignments())
        adapter.addFragment(ClassDashboardClasses())

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

    fun open_announcement_activity(id: String): Unit {

    }
}
