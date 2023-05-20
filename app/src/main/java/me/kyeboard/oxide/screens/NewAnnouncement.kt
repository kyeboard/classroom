package me.kyeboard.oxide.screens

import android.os.Bundle
import android.text.InputFilter
import android.widget.EditText
import androidx.activity.ComponentActivity
import me.kyeboard.oxide.R
import me.kyeboard.oxide.utils.InputFilterMinMax


class NewAnnouncement : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_newannouncement)
    }
}