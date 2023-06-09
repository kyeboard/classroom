package me.kyeboard.classroom.adapters

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ShapeDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import me.kyeboard.classroom.R

data class ClassItem(val name: String, val header: String, val subject: String, val `$id`: String, val color: String, var total: Long)

class ClassesListAdapter(private val dataSet: ArrayList<ClassItem>, private val onClick: (id: String) -> Unit, private val context: Context) :
    RecyclerView.Adapter<ClassesListAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView
        val subject: TextView
        val parent: ConstraintLayout

        init {
            parent = view.findViewById(R.id.classes_list_item_parent)
            title = view.findViewById(R.id.classes_list_item_name)
            subject = view.findViewById(R.id.classes_list_item_subject)
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.classes_list_item, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val data = dataSet[position]

        viewHolder.parent.setOnClickListener {
            onClick(data.`$id`)
        }

        val bg = (viewHolder.parent.background as GradientDrawable)

        bg.setColor(Color.parseColor(data.color))
        bg.setStroke(6, Color.parseColor("#000000"))
        bg.cornerRadius = 5F

        viewHolder.title.text = data.name
        viewHolder.subject.text = data.subject
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

}

