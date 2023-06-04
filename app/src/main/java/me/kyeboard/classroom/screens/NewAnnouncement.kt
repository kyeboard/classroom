package me.kyeboard.classroom.screens

import android.os.Bundle
import android.util.Log
import android.widget.Toast
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

        val class_id = intent.extras!!.getString("class_id")
        Log.e("lmfao", class_id!!)
        val viewPager = findViewById<ViewPager2>(R.id.new_announcement_pager)

        val bundle = Bundle()
        bundle.putString("class_id", class_id)
        val newAnnouncement = CreateNewAnnouncement()
        newAnnouncement.arguments = bundle

        val adapter = ViewPagerAdapter(this)
        adapter.addFragment(newAnnouncement)

        viewPager.adapter = adapter
    }
}