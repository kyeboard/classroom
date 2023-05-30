package me.kyeboard.classroom.screens

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.appwrite.services.Databases
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.kyeboard.classroom.R
import me.kyeboard.classroom.screens.ui.theme.OxideTheme
import me.kyeboard.classroom.utils.get_appwrite_client

data class Assignment(val title: String, val description: String, val attachments: ArrayList<String>, val author: String, val grade: Number, val due_date: String)

class NewAssignment : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_newassignment)

        val client = get_appwrite_client(this)
        val database = Databases(client)

        findViewById<Button>(R.id.newassignment_create_assignment).setOnClickListener {
            val title = findViewById<EditText>(R.id.newassignment_title).text.toString()
            val desc = findViewById<EditText>(R.id.newassignment_description).text.toString()
            val grade = findViewById<EditText>(R.id.newassignment_grade).text.toString().toInt()
            val duedate = findViewById<EditText>(R.id.newassignment_duedate).text.toString()

            if(title.isBlank() || desc.isBlank() || duedate.isBlank()) {
                Toast.makeText(this, "Make sure to fill all the details", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            CoroutineScope(Dispatchers.IO).launch {
                database.createDocument("classes", "646f432ad59caafabf74", "unique()",
                    Assignment(title, desc, arrayListOf(), "kyeboard", grade, duedate)
                )
            }
        }
    }
}
