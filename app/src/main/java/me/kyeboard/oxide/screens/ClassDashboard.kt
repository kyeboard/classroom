package me.kyeboard.oxide.screens

import Announcement
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import io.appwrite.services.Databases
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.kyeboard.oxide.R
import me.kyeboard.oxide.adapters.ViewPagerAdapter
import me.kyeboard.oxide.fragments.ClassDashboardAssignments
import me.kyeboard.oxide.fragments.ClassDashboardClasses
import me.kyeboard.oxide.fragments.ClassDashboardStream
import me.kyeboard.oxide.fragments.CreateNewAnnouncement
import me.kyeboard.oxide.fragments.CreateNewMeeting
import me.kyeboard.oxide.utils.get_appwrite_client

class ClassDashboard : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_classdashboard)

        val viewPager = findViewById<ViewPager2>(R.id.classdashbord_viewpager)
        val tabLayout = findViewById<TabLayout>(R.id.class_dashboard_tablayout)

        val adapter = ViewPagerAdapter(this)
        adapter.addFragment(ClassDashboardStream())
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
