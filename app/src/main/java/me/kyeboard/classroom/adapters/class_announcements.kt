import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import io.appwrite.extensions.tryJsonCast
import io.appwrite.models.Document
import me.kyeboard.classroom.R

data class Announcement(val author: String, val message: String, val classid: String, val attachments: ArrayList<String>, val `$id`: String)

class AnnouncementAdapter(private val dataSet: List<Announcement>, private val onClick: (id: String) -> Unit) :
    RecyclerView.Adapter<AnnouncementAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val username: TextView
        val description: TextView
        val parent: ConstraintLayout
        val time: TextView

        init {
            parent = view.findViewById(R.id.announcement_item_parent)
            username = view.findViewById(R.id.announcement_item_username)
            description = view.findViewById(R.id.announcement_item_description)
            time = view.findViewById(R.id.announcement_item_time)
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.announcement_item_meeting, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val data = dataSet[position]

        viewHolder.description.text = data.message
        viewHolder.username.text = data.author

        viewHolder.parent.setOnClickListener {
            onClick(data.`$id`)
        }

        // Picasso.get().load(data.profile_url).into(viewHolder.user_profile)
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

}


