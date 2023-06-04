package me.kyeboard.classroom.fragments

import AnnouncementAdapter
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.appwrite.Query
import io.appwrite.services.Databases
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.kyeboard.classroom.R
import me.kyeboard.classroom.utils.get_appwrite_client

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ClassDashboardStream.newInstance] factory method to
 * create an instance of this fragment.
 */
class ClassDashboardStream : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_class_dashboard_stream, container, false)

        val recyclerView = view.findViewById<RecyclerView>(R.id.class_dashboard_stream_announcements)
        val class_id = requireArguments().getString("class_id")!!
        val client = get_appwrite_client(view.context)
        val databases = Databases(client)

        Log.d("cc", class_id)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val data = databases.listDocuments("classes", "646c532bc46aecc1120a", arrayListOf(Query.equal("classid", class_id))).documents
                val adapter = AnnouncementAdapter(data)

                activity?.runOnUiThread {
                    recyclerView.adapter = adapter
                    recyclerView.layoutManager = LinearLayoutManager(view.context)
                }
            } catch(e: Exception) {
                Log.e("eee", e.message.toString())
            }
        }

        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ClassDashboardStream().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}