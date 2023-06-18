package me.kyeboard.classroom.screens

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
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
import me.kyeboard.classroom.utils.getAppwriteClient
import me.kyeboard.classroom.utils.setText
import me.kyeboard.classroom.utils.visible

class ClassDashboard : AppCompatActivity() {
    private lateinit var client: Client
    private lateinit var databases: Databases
    private lateinit var teams: Teams
    private var fragmentFetched = false
    private lateinit var dashboardInstance: ClassDashboardStream
    private var activityFetched = false
    private lateinit var account: Account
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var topbar: ConstraintLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        // Setup view
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_classdashboard)

        // Get class id sent along
        val extras = intent.extras!!
        val classId = extras.getString("class_id")!!
        val accentColor = extras.getString("accent_color")!!
        window.statusBarColor = ResourcesCompat.getColor(resources, R.color.bg, theme)

        findViewById<ImageView>(R.id.destory_self).setOnClickListener {
            finish()
        }

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
        viewPager = findViewById(R.id.classdashbord_viewpager)
        tabLayout = findViewById(R.id.class_dashboard_tablayout)
        topbar = findViewById(R.id.class_dashboard_topbar)
        val createNewBtn = findViewById<ImageButton>(R.id.dashboard_stream_create_new)

        // Initiate appwrite services
        client = getAppwriteClient(this)
        databases = Databases(client)
        teams = Teams(client)
        account = Account(client)

        // Setup dashboard stream fragment
        val bundle = Bundle().apply {
            putString("class_id", classId)
            putString("accent_color", accentColor)
        }

        dashboardInstance =  ClassDashboardStream.newInstance {
            fragmentFetched = true
            revealUI()
        }
        val assignmentsInstance = ClassDashboardAssignments()

        dashboardInstance.apply {
            arguments = bundle
        }
        assignmentsInstance.apply {
            arguments = bundle
        }

        val newAnnouncementHandler = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if(it.resultCode == Activity.RESULT_OK) {
                dashboardInstance.updateStreamItems {  }
            }
        }

        val newAssignmentHandler = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if(it.resultCode == Activity.RESULT_OK) {
                assignmentsInstance.populateAssignments(assignmentsInstance.guessFilter()) {  }
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            val roles = teams.listMemberships(classId, arrayListOf(Query.equal("userId", account.get().id))).memberships[0].roles

            if(roles.contains("owner")) {
                runOnUiThread {
                    visible(createNewBtn)
                }
            }

            try {
                // Get class info from registry
                val classInfo = databases.getDocument("classes", "registry", classId).data.tryJsonCast<ClassItem>()!!

                runOnUiThread {
                    // Set background color
                    topbar.background.mutate().apply {
                        setTint(Color.parseColor(classInfo.color))
                    }
                    window.statusBarColor = Color.parseColor(accentColor)

                    // Handle plus button clicks
                    createNewBtn.setOnClickListener {
                        // Select the intent to show
                        val intent = if(viewPager.currentItem == 0) {
                            Intent(this@ClassDashboard, NewAnnouncement::class.java)
                        } else {
                            Intent(this@ClassDashboard, NewAssignment::class.java)
                        }

                        // Add class id
                        intent.putExtra("class_id", classId)
                        intent.putExtra("accent_color", classInfo.color)

                        if(viewPager.currentItem == 0) {
                            newAnnouncementHandler.launch(intent)
                        } else {
                            newAssignmentHandler.launch(intent)
                        }
                    }

                    // Change name and subject
                    setText(this@ClassDashboard, R.id.current_class_name, classInfo.name)
                    setText(this@ClassDashboard, R.id.current_class_subject, classInfo.subject)

                    activityFetched = true
                    revealUI()
                }
            } catch(e: Exception) {
                Log.e("class_info", e.message.toString())

                runOnUiThread {
                    Toast.makeText(this@ClassDashboard, "Cannot find the class", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Setup pager adapter
        val adapter = ViewPagerAdapter(this)
        adapter.addFragment(dashboardInstance)
        adapter.addFragment(assignmentsInstance)

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

    private fun revealUI() {
        if(fragmentFetched && activityFetched) {
            visible(topbar)
            visible(tabLayout)
            visible(findViewById(R.id.class_dashboard_bottom_bar))

            dashboardInstance.recyclerView.animate().alpha(1f).duration = 100
        }
    }
}
