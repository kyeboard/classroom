package me.kyeboard.classroom.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import me.kyeboard.classroom.R

data class Attachment(val file_extension: String, val file_name: String)

class AttachmentAdapter(private val dataSet: ArrayList<Attachment>) :
    RecyclerView.Adapter<AttachmentAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val extension: TextView
        val name: TextView

        init {
            extension = view.findViewById(R.id.new_announcement_attachment_item_extension)
            name = view.findViewById(R.id.new_announcement_attachment_item_name)
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.new_announcement_attachment_item, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val data = dataSet[position]

        viewHolder.extension.text = data.file_extension
        viewHolder.name.text = data.file_name
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

}

