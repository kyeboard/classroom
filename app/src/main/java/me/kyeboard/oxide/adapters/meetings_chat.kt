import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import io.appwrite.extensions.tryJsonCast
import io.appwrite.models.Document
import me.kyeboard.oxide.R

data class Chat(val author: String, val message: String)

class ChatsAdapter(private val dataSet: ArrayList<Chat>) : RecyclerView.Adapter<ChatsAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val username_placeholder: TextView
        val message_placeholder: TextView

        init {
            username_placeholder = view.findViewById(R.id.meetings_chat_item_username)
            message_placeholder = view.findViewById(R.id.meetings_chat_item_message)
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.meeting_chat_item, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val data = dataSet[position]

        viewHolder.username_placeholder.text = data.author
        viewHolder.message_placeholder.text = data.message
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

}

