package me.kyeboard.classroom.screens

import MembersList
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import io.appwrite.extensions.tryJsonCast
import io.appwrite.services.Databases
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.kyeboard.classroom.R
import me.kyeboard.classroom.adapters.ViewPagerAdapter
import me.kyeboard.classroom.fragments.ClassDashboardAssignments
import me.kyeboard.classroom.fragments.ClassDashboardStream
import me.kyeboard.classroom.utils.get_appwrite_client

class ClassDashboard : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Setup view
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_classdashboard)

        window.statusBarColor = ResourcesCompat.getColor(resources, R.color.bg, null)

        // Get class id sent along
        val classId = intent.extras!!.getString("class_id")!!
        val accent_color = intent.extras!!.getString("accent_color")!!

        // Handle members list opener
        findViewById<ImageView>(R.id.class_members_open).setOnClickListener {
            // Create an intent
            val intent = Intent(this, ClassPeople::class.java)

            // Add class id
            intent.putExtra("class_id", classId)
            intent.putExtra("accent_color", accent_color)

            // Start the intent
            startActivity(intent)
        }

        // Viewpager and tab layout for nav
        val viewPager = findViewById<ViewPager2>(R.id.classdashbord_viewpager)
        val tabLayout = findViewById<TabLayout>(R.id.class_dashboard_tablayout)

        // Initiate appwrite services
        val client = get_appwrite_client(this)
        val databases = Databases(client)

        CoroutineScope(Dispatchers.IO).launch {
            // Get class info from registery
            val classInfo = databases.getDocument("classes", "registery", classId).data.tryJsonCast<ClassItem>()!!

            runOnUiThread {
                // Change tint of the topbar
                val topbar = findViewById<ConstraintLayout>(R.id.class_dashboard_topbar)
                topbar.background.apply {
                    setTint(Color.parseColor(classInfo.color))
                }

                // Handle plus button clicks
                findViewById<ImageButton>(R.id.class_dashboard_new_announcement).setOnClickListener {
                    // Select the intent to show
                    val intent = if(viewPager.currentItem == 0) {
                        Intent(this@ClassDashboard, NewAnnouncement::class.java)
                    } else {
                        Intent(this@ClassDashboard, NewAssignment::class.java)
                    }

                    // Add class id
                    intent.putExtra("class_id", classId)
                    intent.putExtra("accent_color", classInfo.color)

                    // Start
                    startActivity(intent)
                }

                // Change name and subject
                findViewById<TextView>(R.id.current_class_name).text = classInfo.name
                findViewById<TextView>(R.id.current_class_subject).text = classInfo.subject

                topbar.visibility = View.VISIBLE
                tabLayout.visibility = View.VISIBLE
                findViewById<View>(R.id.class_dashboard_bottom_bar).visibility = View.VISIBLE

                // Change the status bar color according to the selected accent color
                window.statusBarColor = Color.parseColor(classInfo.color)
            }
        }

        // Setup dashboard stream fragment
        val bundle = Bundle().apply {
            putString("class_id", classId)
        }
        val classDashboardStream = ClassDashboardStream().apply {
            arguments = bundle
        }
        val classDashboardAssignments = ClassDashboardAssignments().apply {
            arguments = bundle
        }

        // Setup pager adapter
        val adapter = ViewPagerAdapter(this)
        adapter.addFragment(classDashboardStream)
        adapter.addFragment(classDashboardAssignments)

        // Set the adapter
        viewPager.adapter = adapter

        // Change view pager position according to tab clicks
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    viewPager.setCurrentItem(it.position, true)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }
        })
    }
}
