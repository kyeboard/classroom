import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import io.appwrite.extensions.tryJsonCast
import io.appwrite.models.Document
import me.kyeboard.oxide.R

data class Announcement(val profile_url: String, val author: String, val content: String)

class AnnouncementsAdapter(private val dataSet: List<Document>, private val onClick: (id: String) -> Unit) :
    RecyclerView.Adapter<AnnouncementsAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // val user_profile: ImageView

        init {
            // user_profile = view.findViewById(R.id.announcement_item_author_pfp)
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
        val data = dataSet[position].data.tryJsonCast<Announcement>()!!

        // Picasso.get().load(data.profile_url).into(viewHolder.user_profile)
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

}

