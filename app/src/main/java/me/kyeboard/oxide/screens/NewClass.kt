package me.kyeboard.oxide.screens

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
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
import me.kyeboard.oxide.R
import me.kyeboard.oxide.screens.ui.theme.OxideTheme

class NewClass : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_newclass)

        // Initial
        update_header_image("https://cloud.appwrite.io/v1/storage/buckets/646460e48963e000edd6/files/landscape1/view?project=fryday")

        val headerHandler = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            try {
                update_header_image(result.data?.data!!.toString())
            } catch(e: Exception) {

            }
        }

        findViewById<CardView>(R.id.select_header).setOnClickListener {
            val intent = Intent(this, SelectHeader::class.java)
            headerHandler.launch(intent)
        }
    }

    fun update_header_image(url: String) {
        Picasso.get().load(url).into(findViewById<ImageView>(R.id.selected_header_preview))
    }
}
