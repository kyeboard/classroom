package me.kyeboard.classroom.screens

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import me.kyeboard.classroom.R
import me.kyeboard.classroom.adapters.ViewPagerAdapter
import me.kyeboard.classroom.fragments.CreateNewAnnouncement
import me.kyeboard.classroom.fragments.CreateNewMeeting


class NewAnnouncement : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_newannouncement)

        val viewPager = findViewById<ViewPager2>(R.id.new_announcement_pager)
        val tabLayout = findViewById<TabLayout>(R.id.new_announcement_tabs)

        val adapter = ViewPagerAdapter(this)
        adapter.addFragment(CreateNewAnnouncement())
        adapter.addFragment(CreateNewMeeting())

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