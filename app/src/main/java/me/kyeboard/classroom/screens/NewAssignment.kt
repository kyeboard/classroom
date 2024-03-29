package me.kyeboard.classroom.screens

import android.app.Activity
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.appwrite.services.Account
import io.appwrite.services.Databases
import io.appwrite.services.Storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.kyeboard.classroom.R
import me.kyeboard.classroom.adapters.Attachment
import me.kyeboard.classroom.adapters.AttachmentAdapter
import me.kyeboard.classroom.utils.getAppwriteClient
import me.kyeboard.classroom.utils.getFileName
import me.kyeboard.classroom.utils.uploadToAppwriteStorage
import me.kyeboard.classroom.utils.visible
import java.util.Calendar
import java.util.Date


data class Assignment(val title: String, val description: String, val attachments: ArrayList<String>, val author: String, val grade: Number, val due_date: String, val classid: String, val authorId: String)

class NewAssignment : ComponentActivity() {
    private val attachments: ArrayList<Attachment> = arrayListOf()
    private val attachmentsUri: ArrayList<Uri> = arrayListOf()
    private lateinit var dueDateInput: EditText

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDayOfMonth ->
            // Do something with the selected date
            val selectedDate = "$selectedYear-${selectedMonth + 1}-$selectedDayOfMonth"
            dueDateInput.setText(selectedDate)
            // You can display the selected date in a TextView or perform any other action
        }, year, month, dayOfMonth)

        datePickerDialog.show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Setup view
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_newassignment)

        dueDateInput = findViewById<EditText>(R.id.newassignment_duedate)

        // Get items from bundle
        val classId = intent.extras!!.getString("class_id")!!
        val accentColor = intent.extras!!.getString("accent_color")!!

        // View items
        val recyclerView = findViewById<RecyclerView>(R.id.new_assignment_attachments)
        val loading = findViewById<ConstraintLayout>(R.id.newassignment_loading)

        // Remove status bar
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val today = Date()
        var due_date_year = today.year
        var due_date_month = today.month
        var due_date_day = today.day

        val myDateListener = OnDateSetListener { arg0, arg1, arg2, arg3 ->
            due_date_year = arg1
            due_date_month = arg2
            due_date_day = arg3


        }

        findViewById<View>(R.id.assignment_duedate_input_handler).setOnClickListener {
            showDatePickerDialog()
        }

        // Set accent
        (findViewById<ConstraintLayout>(R.id.newassignment_topbar).background.mutate() as GradientDrawable).apply {
            setTint(android.graphics.Color.parseColor(accentColor))
        }

        (findViewById<Button>(R.id.newassignment_create_assignment).background.mutate() as GradientDrawable).apply {
            setColor(android.graphics.Color.parseColor(accentColor))
            setStroke(5, android.graphics.Color.parseColor("#000000"))
            cornerRadius = 5F
        }

        // Adapters
        val adapter = AttachmentAdapter(attachments)

        // Set finish listener
        findViewById<ImageView>(R.id.destroy_self).setOnClickListener {
            finish()
        }

        // Add adapters
        recyclerView.adapter = adapter
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        // Initialize appwrite services
        val client = getAppwriteClient(this)
        val database = Databases(client)
        val storage = Storage(client)
        val account = Account(client)

        // Handle input
        val intent = Intent().apply {
            action = Intent.ACTION_GET_CONTENT
            type = "*/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }

        // Handle picking ip files
        val pickFiles = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if(result.resultCode == Activity.RESULT_OK) {
                if(result.data != null) {
                    val clipData = result.data!!.clipData

                    if(clipData != null) {
                        for (index in 0 until clipData.itemCount) {
                            val uri: Uri = clipData.getItemAt(index).uri

                            val fileName = getFileName(this.contentResolver, uri)
                            attachments.add(Attachment(fileName.substringAfterLast('.', ""), fileName))
                            attachmentsUri.add(uri)
                        }

                        adapter.notifyItemRangeChanged(attachments.size - 1, clipData.itemCount)
                    } else {
                        val contentsURI = result.data?.data!!
                        val fileName = getFileName(this.contentResolver, contentsURI)

                        attachments.add(Attachment(fileName.substringAfterLast('.', ""), fileName))
                        attachmentsUri.add(contentsURI)

                        adapter.notifyItemChanged(attachments.size - 1)
                    }
                }
            }
        }

        // Add attachment listener
        findViewById<Button>(R.id.new_assignment_add_attachment).setOnClickListener {
            pickFiles.launch(intent)
        }

        findViewById<Button>(R.id.newassignment_create_assignment).setOnClickListener {
            val title = findViewById<EditText>(R.id.newassignment_title).text.toString()
            val desc = findViewById<EditText>(R.id.newassignment_description).text.toString()
            val grade = findViewById<EditText>(R.id.newassignment_grade).text.toString().toInt()
            val duedate = dueDateInput.text.toString()

            if(title.isBlank() || desc.isBlank() || duedate.isBlank()) {
                Toast.makeText(this, "Make sure to fill all the details", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loading.alpha = 0f
            visible(loading)
            loading.animate().alpha(1f).duration = 200

            val attachment_ids = arrayListOf<String>()

            CoroutineScope(Dispatchers.IO).launch {
                val user = account.get()

                for(uri in attachmentsUri) {
                    attachment_ids.add(uploadToAppwriteStorage(contentResolver, uri, storage))
                }

                database.createDocument("classes", "assignments", "unique()",
                    Assignment(title, desc, attachment_ids, user.name, grade, duedate, classId, user.id)
                )

                setResult(Activity.RESULT_OK)

                finish()
            }
        }
    }
}
