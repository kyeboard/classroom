import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import io.appwrite.models.Membership
import me.kyeboard.classroom.R

class SubmissionsAdapter(private val dataSet: List<Membership>, val onClick: (id: String) -> Unit) :
    RecyclerView.Adapter<SubmissionsAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val username: TextView
        val parent: ConstraintLayout

        init {
            username = view.findViewById(R.id.submission_item_view_name)
            parent = view.findViewById(R.id.submissions_view_parent)
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.submission_item_view, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val data = dataSet[position]

        viewHolder.username.text = data.userName

        viewHolder.parent.setOnClickListener {
            onClick(data.userId)
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size
}


