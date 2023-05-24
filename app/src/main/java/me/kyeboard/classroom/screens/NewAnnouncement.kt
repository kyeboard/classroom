package me.kyeboard.oxide.screens

import android.os.Bundle
import android.text.InputFilter
import android.util.Log
import android.widget.EditText
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import me.kyeboard.oxide.R
import me.kyeboard.oxide.adapters.ViewPagerAdapter
import me.kyeboard.oxide.fragments.CreateNewAnnouncement
import me.kyeboard.oxide.fragments.CreateNewMeeting
import me.kyeboard.oxide.utils.InputFilterMinMax


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
                Log.d("test", "testtr")
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