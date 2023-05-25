import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.appwrite.extensions.tryJsonCast
import io.appwrite.models.Document
import me.kyeboard.classroom.R

data class Assignment(val author: String, val description: String)

class AssignmentAdapter(private val dataSet: List<Document>) :
    RecyclerView.Adapter<AssignmentAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val username: TextView
        val description: TextView
        init {
            username = view.findViewById(R.id.announcement_item_username)
            description = view.findViewById(R.id.announcement_item_description)
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.class_assignments_item, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val data = dataSet[position].data.tryJsonCast<Announcement>()!!

        viewHolder.description.text = data.description
        viewHolder.username.text = data.author
        // viewHolder.time.text = getRelativeTimeSpanString()

        // Picasso.get().load(data.profile_url).into(viewHolder.user_profile)
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

}

