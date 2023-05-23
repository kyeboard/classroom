package me.kyeboard.oxide.screens

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.squareup.picasso.Picasso
import io.appwrite.services.Databases
import io.appwrite.services.Teams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.kyeboard.oxide.R
import me.kyeboard.oxide.adapters.ClassItem
import me.kyeboard.oxide.screens.ui.theme.OxideTheme
import me.kyeboard.oxide.utils.get_appwrite_client

class NewClass : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_newclass)

        var header_image = "https://cloud.appwrite.io/v1/storage/buckets/646460e48963e000edd6/files/landscape1/view?project=fryday"
        val client = get_appwrite_client(this)
        val teams = Teams(client)
        val database = Databases(client)

        // Initial
        updateHeaderImage(header_image)

        val headerHandler = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            try {
                header_image = result.data?.data!!.toString()
                updateHeaderImage(header_image)
            } catch(_: Exception) {

            }
        }

        findViewById<Button>(R.id.new_class_create).setOnClickListener {
            val class_name = findViewById<EditText>(R.id.new_class_name).text.toString()
            val class_subject = findViewById<EditText>(R.id.new_class_subject).text.toString()

            if (class_name.isBlank() || class_subject.isBlank()) {
                Toast.makeText(this, "Fill in all details before continuing", Toast.LENGTH_SHORT).show()

                return@setOnClickListener
            }

            // Create a team
            CoroutineScope(Dispatchers.IO).launch {
                runOnUiThread {
                    Toast.makeText(this@NewClass, "Renning", Toast.LENGTH_SHORT).show()
                }
                val team = teams.create("unique()", class_name)

                database.createDocument("classes", "registery", team.id, ClassItem(class_name, header_image, class_subject))
            }
        }

        findViewById<CardView>(R.id.select_header).setOnClickListener {
            val intent = Intent(this, SelectHeader::class.java)
            headerHandler.launch(intent)
        }
    }

    private fun updateHeaderImage(url: String) {
        Picasso.get().load(url).into(findViewById<ImageView>(R.id.selected_header_preview))
    }
}
