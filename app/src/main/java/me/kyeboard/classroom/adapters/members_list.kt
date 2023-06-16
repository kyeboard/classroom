import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import io.appwrite.models.Membership
import me.kyeboard.classroom.R
import java.text.SimpleDateFormat
import java.util.Locale

val memberInputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.US)
val memberOutputFormat = SimpleDateFormat("d MMMM, yyyy", Locale.US)

class MembersList(private val dataSet: List<Membership>, private val isOwner: Boolean, private val onRemove: (membershipId: String, position: Int) -> Unit) :
    RecyclerView.Adapter<MembersList.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val username: TextView
        val pfp: ImageView
        val memberSince: TextView
        val remove: ImageView

        init {
            username = view.findViewById(R.id.members_list_item_username)
            pfp = view.findViewById(R.id.member_list_item_pfp)
            memberSince = view.findViewById(R.id.members_list_item_member_since)
            remove = view.findViewById(R.id.remove_member)
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.members_list_item, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val data = dataSet[position]

        viewHolder.username.text = data.userName

        if(data.confirm) {
            val date = memberInputFormat.parse(data.joined)!!
            viewHolder.memberSince.text = "Member since ${memberOutputFormat.format(date)}"
        } else {
            val date = memberInputFormat.parse(data.invited)!!
            viewHolder.memberSince.text = "Not joined yet | Invited on ${memberOutputFormat.format(date)}"
        }

        if(isOwner) {
            viewHolder.remove.setOnClickListener {
                onRemove(data.id, position)
            }
        } else {
            viewHolder.remove.visibility = View.GONE
        }

        Picasso.get().load("https://cloud.appwrite.io/v1/storage/buckets/userpfps/files/${data.userId}/view?project=classroom").into(viewHolder.pfp)
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size
}


