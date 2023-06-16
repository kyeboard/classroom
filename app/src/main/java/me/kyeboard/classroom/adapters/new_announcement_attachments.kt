package me.kyeboard.classroom.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import me.kyeboard.classroom.R
import me.kyeboard.classroom.utils.visible

data class Attachment(val file_extension: String, val file_name: String, val onClick: () -> Unit = {  })

class AttachmentAdapter(private val dataSet: ArrayList<Attachment>, private val allowRemoving: Boolean = false, private val onRemoveCallback: (Attachment, Int) -> Unit = { _, _ -> }) :
    RecyclerView.Adapter<AttachmentAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val extension: TextView
        val parent: ConstraintLayout
        val name: TextView
        val remove: ImageView

        init {
            extension = view.findViewById(R.id.new_announcement_attachment_item_extension)
            name = view.findViewById(R.id.new_announcement_attachment_item_name)
            parent = view.findViewById(R.id.attachment_item_parent)
            remove = view.findViewById(R.id.removeAttachment)
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.new_announcement_attachment_item, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val data = dataSet[position]

        if(allowRemoving) {
            visible(viewHolder.remove)

            viewHolder.remove.setOnClickListener {
                onRemoveCallback(data, position)
            }
        }

        viewHolder.extension.text = data.file_extension
        viewHolder.name.text = data.file_name

        viewHolder.parent.setOnClickListener {
            data.onClick()
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

}

