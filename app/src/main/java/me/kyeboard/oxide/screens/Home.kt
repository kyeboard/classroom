package me.kyeboard.oxide.screens

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import me.kyeboard.oxide.R
import me.kyeboard.oxide.screens.ui.theme.OxideTheme

class Home : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        findViewById<ImageButton>(R.id.open_new_class_popup).setOnClickListener {
            // Create a new intent
            val intent = Intent(this, NewAnnouncement::class.java)

            // Start the intent
            startActivity(intent)
        }
    }
}