
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import io.appwrite.models.Membership
import me.kyeboard.classroom.R
import me.kyeboard.classroom.utils.imageIntoWidget

class SubmissionsAdapter(private val dataSet: List<Membership>, val submission_info: ArrayList<Boolean>, val onClick: (id: String) -> Unit) :
    RecyclerView.Adapter<SubmissionsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val username: TextView
        val pfp: ImageView
        val parent: ConstraintLayout
        val has_submitted: TextView

        init {
            username = view.findViewById(R.id.submission_item_view_name)
            parent = view.findViewById(R.id.submissions_view_parent)
            pfp = view.findViewById(R.id.submission_item_view_pfp)
            has_submitted = view.findViewById(R.id.has_submitted)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.submission_item_view, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val data = dataSet[position]

        viewHolder.username.text = data.userName
        imageIntoWidget(viewHolder.pfp, "https://cloud.appwrite.io/v1/storage/buckets/userpfps/files/648abf4cbc7972c48267/view?project=classroom")

        if(submission_info[position]) {
            viewHolder.has_submitted.text = "Submitted"

            viewHolder.parent.setOnClickListener {
                onClick(data.userId)
            }
        } else {
            viewHolder.has_submitted.text = "Not submitted"
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size
}


