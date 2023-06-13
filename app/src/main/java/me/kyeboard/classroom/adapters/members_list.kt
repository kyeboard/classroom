import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.os.StrictMode
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import io.appwrite.models.Membership
import io.appwrite.services.Storage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.kyeboard.classroom.R
import me.kyeboard.classroom.utils.get_appwrite_client
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

class MembersList(private val dataSet: List<Membership>) :
    RecyclerView.Adapter<MembersList.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val username: TextView
        val pfp: ImageView
        val member_since: TextView

        init {
            username = view.findViewById(R.id.members_list_item_username)
            pfp = view.findViewById(R.id.member_list_item_pfp)
            member_since = view.findViewById(R.id.members_list_item_member_since)
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
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.US)
            val outputFormat = SimpleDateFormat("d MMMM, yyyy", Locale.US)

            val date = inputFormat.parse(data.joined)!!

            viewHolder.member_since.text = "Member since ${outputFormat.format(date)}"
        } else {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.US)
            val outputFormat = SimpleDateFormat("d MMMM, yyyy", Locale.US)

            val date = inputFormat.parse(data.invited)!!

            viewHolder.member_since.text = "Not joined yet | Invited on ${outputFormat.format(date)}"
        }

        Picasso.get().load("https://cloud.appwrite.io/v1/storage/buckets/646ef17593d213adfcf2/files/${data.userId}/view?project=fryday").into(viewHolder.pfp)
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size
}


