import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.os.StrictMode
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import io.appwrite.models.Membership
import io.appwrite.services.Storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.kyeboard.classroom.R
import me.kyeboard.classroom.utils.get_appwrite_client
import java.io.File
import java.io.FileOutputStream

class SubmissionsAdapter(private val dataSet: List<Membership>) :
    RecyclerView.Adapter<SubmissionsAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val username: TextView
        val submitted_at: TextView
        val parent: ConstraintLayout

        init {
            username = view.findViewById(R.id.submission_item_view_name)
            submitted_at = view.findViewById(R.id.submission_item_view_submitted_at)
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
        val context = viewHolder.parent.context

        viewHolder.parent.setOnClickListener {
            val client = get_appwrite_client(context)
            val storage = Storage(client)

            GlobalScope.launch {
                val submission_stream = storage.getFileDownload("647713fa9be2a68d4458", "4535baebf4f408c62116a7cbfefaca3e")
                val submission_info = storage.getFile("647713fa9be2a68d4458", "4535baebf4f408c62116a7cbfefaca3e")

                val extDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                Log.d("tt", extDirectory.absolutePath)

                val folder = File(extDirectory, "classroom")
                folder.mkdirs()

                val file = File(folder, "assignment.pdf")
                val file_stream = FileOutputStream(file)

                file_stream.write(submission_stream)

                // TODO: REMOVE THIS IN PRODUCTION
                val builder = StrictMode.VmPolicy.Builder()
                StrictMode.setVmPolicy(builder.build())

                val intent = Intent(Intent.ACTION_VIEW)
                intent.setDataAndType(Uri.fromFile(file), "application/pdf")
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                context.startActivity(intent)
            }
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size
}


