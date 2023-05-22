package me.kyeboard.oxide.screens

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import me.kyeboard.oxide.R
import me.kyeboard.oxide.adapters.HeadersListAdapter

class SelectHeader : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.select_header)

        val headers = arrayListOf(
            "https://cloud.appwrite.io/v1/storage/buckets/646460e48963e000edd6/files/landscape1/view?project=fryday",
            "https://cloud.appwrite.io/v1/storage/buckets/646460e48963e000edd6/files/landscape2/view?project=fryday",
            "https://cloud.appwrite.io/v1/storage/buckets/646460e48963e000edd6/files/landscape3/view?project=fryday",
            "https://cloud.appwrite.io/v1/storage/buckets/646460e48963e000edd6/files/landscape4/view?project=fryday",
            "https://cloud.appwrite.io/v1/storage/buckets/646460e48963e000edd6/files/landscape5/view?project=fryday",
            "https://cloud.appwrite.io/v1/storage/buckets/646460e48963e000edd6/files/landscape6/view?project=fryday",
            "https://cloud.appwrite.io/v1/storage/buckets/646460e48963e000edd6/files/landscape7/view?project=fryday",
            "https://cloud.appwrite.io/v1/storage/buckets/646460e48963e000edd6/files/landscape8/view?project=fryday",
            "https://cloud.appwrite.io/v1/storage/buckets/646460e48963e000edd6/files/landscape9/view?project=fryday",
            "https://cloud.appwrite.io/v1/storage/buckets/646460e48963e000edd6/files/landscape10/view?project=fryday",
            "https://cloud.appwrite.io/v1/storage/buckets/646460e48963e000edd6/files/landscape11/view?project=fryday",
        )

        val adapter = HeadersListAdapter(headers, this::send_result)
        val listview = findViewById<RecyclerView>(R.id.select_header_list)

        listview.adapter = adapter
        listview.layoutManager = LinearLayoutManager(this)
    }

    fun send_result(url: String): Unit {
        val v = Intent()

        v.data = Uri.parse(url)

        setResult(RESULT_OK, v)

        finish()
    }
}
