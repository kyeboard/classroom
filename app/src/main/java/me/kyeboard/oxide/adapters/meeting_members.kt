package me.kyeboard.oxide.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.appwrite.extensions.tryJsonCast
import me.kyeboard.oxide.R
import com.squareup.picasso.Picasso;


data class Member(val name: String, val pfp: String)

class MeetingMembersAdapter(private val dataSet: ArrayList<Any>) : RecyclerView.Adapter<MeetingMembersAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val meeting_member_name: TextView
        val meeting_member_pfp: ImageView

        init {
            // Define click listener for the ViewHolder's View
            meeting_member_name = view.findViewById(R.id.meeting_member_card_name)
            meeting_member_pfp = view.findViewById(R.id.meeting_member_card_pfp)
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.meeting_member_card, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val item = dataSet[position].tryJsonCast<Member>()!!

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.meeting_member_name.text = item.name
        Picasso.get().load(item.pfp).into(viewHolder.meeting_member_pfp)
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size
}
