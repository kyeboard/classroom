package me.kyeboard.classroom.fragments

import android.os.Bundle
import android.text.InputFilter.LengthFilter
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import io.appwrite.services.Databases
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.kyeboard.classroom.R
import me.kyeboard.classroom.utils.get_appwrite_client

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CreateNewAnnouncement.newInstance] factory method to
 * create an instance of this fragment.
 */

data class AnnouncementItem(val author: String, val description: String)

class CreateNewAnnouncement : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.create_new_announcement, container, false)
        val client = get_appwrite_client(view.context)
        val databases = Databases(client)

        view.findViewById<Button>(R.id.new_announcement_create_announcement).setOnClickListener {
            val message = view.findViewById<EditText>(R.id.new_announcement_message).text.toString()

            if(message.isBlank()) {
                Toast.makeText(view.context, "Fill in all details before submitting", Toast.LENGTH_SHORT).show()
            }

            CoroutineScope(Dispatchers.IO).launch {
                databases.createDocument("classes", "646c532bc46aecc1120a", "unique()", AnnouncementItem("kyeboard", message))
            }
        }

        return view
    }
}