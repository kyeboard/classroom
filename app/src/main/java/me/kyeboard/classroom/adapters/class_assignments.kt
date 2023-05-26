import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.appwrite.extensions.tryJsonCast
import io.appwrite.models.Document
import me.kyeboard.classroom.R
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

data class Assignment(val author: String, val title: String, val due_date: String)

val isoDateFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")

class AssignmentAdapter(private val dataSet: List<Document>) :
    RecyclerView.Adapter<AssignmentAdapter.ViewHolder>() {

    var previousAssignmentDate = Date()

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView
        val time: TextView
        val timeDiff: TextView

        init {
            title = view.findViewById(R.id.class_assignment_item_title)
            time = view.findViewById(R.id.class_assignment_item_time)
            timeDiff = view.findViewById(R.id.class_assignments_time_header)
        }
    }

    private fun getDayDifference(date: String): Number {
        val date = isoDateFormatter.parse(date)!!

        val cal = Calendar.getInstance()
        cal.time = date

        Log.d("today", "$date and the prev $previousAssignmentDate")

        return cal.get(Calendar.DAY_OF_MONTH) - previousAssignmentDate.date
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
        val data = dataSet[position].data.tryJsonCast<Assignment>()!!

        val diff = getDayDifference(data.due_date)

        viewHolder.title.text = data.title
        viewHolder.time.text = dataSet[position].createdAt
        viewHolder.timeDiff.text = "$diff hours ago"
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

}

