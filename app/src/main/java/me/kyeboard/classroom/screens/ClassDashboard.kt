package me.kyeboard.classroom.screens

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import io.appwrite.extensions.tryJsonCast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.kyeboard.classroom.R
import me.kyeboard.classroom.adapters.ViewPagerAdapter
import me.kyeboard.classroom.fragments.ClassDashboardAssignments
import me.kyeboard.classroom.fragments.ClassDashboardClasses
import me.kyeboard.classroom.fragments.ClassDashboardStream
import me.kyeboard.classroom.utils.AppwriteServiceSingleton

class ClassDashboard : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_classdashboard)

        // val class_id = intent.extras!!.getString("class_id")
        val class_id = "647da6bb4463f64ae8f5"

        val service = AppwriteServiceSingleton.getInstance(this).get()!!

        CoroutineScope(Dispatchers.IO).launch {
            val class_info = service.databases.getDocument("classes", "registery", class_id).data.tryJsonCast<ClassItem>()!!

            window.statusBarColor = Color.parseColor(class_info.color)

            runOnUiThread {
                findViewById<ConstraintLayout>(R.id.class_dashboard_topbar).background.apply {
                    setTint(Color.parseColor(class_info.color))
                }
                findViewById<TextView>(R.id.current_class_name).text = class_info.name
                findViewById<TextView>(R.id.current_class_subject).text = class_info.subject
            }
        }

        val viewPager = findViewById<ViewPager2>(R.id.classdashbord_viewpager)
        val tabLayout = findViewById<TabLayout>(R.id.class_dashboard_tablayout)

        val bundle = Bundle().apply {
            putString("class_id", class_id)
        }
        val classDashboardStream = ClassDashboardStream().apply {
            arguments = bundle
        }

        findViewById<ImageButton>(R.id.class_dashboard_new_announcement).setOnClickListener {
            val intent = if(viewPager.currentItem == 0) {
                Intent(this, NewAnnouncement::class.java)
            } else {
                Intent(this, NewAssignment::class.java)
            }
            intent.putExtra("class_id", class_id)
            startActivity(intent)
        }

        val adapter = ViewPagerAdapter(this)
        adapter.addFragment(classDashboardStream)
        adapter.addFragment(ClassDashboardAssignments())

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
