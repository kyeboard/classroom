package me.kyeboard.classroom.screens

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.WindowCompat
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.android.material.tabs.TabLayout
import io.appwrite.Client
import io.appwrite.Query
import io.appwrite.extensions.tryJsonCast
import io.appwrite.services.Account
import io.appwrite.services.Databases
import io.appwrite.services.Teams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.kyeboard.classroom.R
import me.kyeboard.classroom.adapters.ViewPagerAdapter
import me.kyeboard.classroom.fragments.ClassDashboardAssignments
import me.kyeboard.classroom.fragments.ClassDashboardStream
import me.kyeboard.classroom.utils.get_appwrite_client
import me.kyeboard.classroom.utils.invisible
import me.kyeboard.classroom.utils.setText
import me.kyeboard.classroom.utils.visible

class ClassDashboard : AppCompatActivity() {
    private lateinit var client: Client
    private lateinit var databases: Databases
    private lateinit var teams: Teams
    private lateinit var account: Account

    override fun onCreate(savedInstanceState: Bundle?) {
        // Setup view
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_classdashboard)

        // Remove status bar (make it transparent)
        WindowCompat.setDecorFitsSystemWindows(
            window,
            false
        )

        // Get class id sent along
        val extras = intent.extras!!
        val classId = extras.getString("class_id")!!
        val accentColor = extras.getString("accent_color")!!

        // Handle members list opener
        findViewById<ImageView>(R.id.class_members_open).setOnClickListener {
            // Create an intent
            val intent = Intent(this, ClassPeople::class.java)

            // Add class id
            intent.putExtra("class_id", classId)
            intent.putExtra("accent_color", accentColor)

            // Start the intent
            startActivity(intent)
        }

        // Viewpager and tab layout for nav
        val viewPager = findViewById<ViewPager2>(R.id.classdashbord_viewpager)
        val tabLayout = findViewById<TabLayout>(R.id.class_dashboard_tablayout)
        val create_new_btn = findViewById<ImageButton>(R.id.dashboard_stream_create_new)

        // Initiate appwrite services
        client = get_appwrite_client(this)
        databases = Databases(client)
        teams = Teams(client)
        account = Account(client)

        CoroutineScope(Dispatchers.IO).launch {
            val roles = teams.listMemberships(classId, arrayListOf(Query.equal("userId", account.get().id))).memberships[0].roles

            if(roles.contains("owner")) {
                runOnUiThread {
                    visible(create_new_btn)
                }
            }

            // Get class info from registery
            val classInfo = databases.getDocument("classes", "registery", classId).data.tryJsonCast<ClassItem>()!!

            runOnUiThread {
                // Change tint of the topbar
                val topbar = findViewById<ConstraintLayout>(R.id.class_dashboard_topbar)

                // Set background color
                topbar.background.mutate().apply {
                    setTint(Color.parseColor(classInfo.color))
                }

                // Handle plus button clicks
                create_new_btn.setOnClickListener {
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
                setText(this@ClassDashboard, R.id.current_class_name, classInfo.name)
                setText(this@ClassDashboard, R.id.current_class_subject, classInfo.subject)

                visible(topbar)
                visible(tabLayout)
                visible(findViewById(R.id.class_dashboard_bottom_bar))
            }
        }

        // Setup dashboard stream fragment
        val bundle = Bundle().apply {
            putString("class_id", classId)
            putString("accent_color", accentColor)
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

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // Handle changes in viewpager
        viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                tabLayout.getTabAt(position)!!.select()
            }
        })
    }
}
