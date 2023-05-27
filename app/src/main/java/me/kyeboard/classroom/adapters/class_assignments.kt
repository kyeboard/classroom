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
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit
import kotlin.math.abs

data class Assignment(val author: String, val title: String, val due_date: String)

val isoDateFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")

class AssignmentAdapter(private val dataSet: List<Document>) :
    RecyclerView.Adapter<AssignmentAdapter.ViewHolder>() {

    var previousDiff = -1
    val today = Date()

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

    private fun getDayDifference(date: String): Int {
        val date = isoDateFormatter.parse(date)!!

        val cal = Calendar.getInstance()
        cal.time = date

        return cal.get(Calendar.DAY_OF_MONTH) - today.date
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.class_assignments_item, viewGroup, false)

        return ViewHolder(view)
    }

    fun getHumanReadableTimeDifference(date: String): String {
        val diff = getDayDifference(date)

        return when {
            diff == 0 -> "Today"
            diff == -1 -> "Yesterday"
            diff >= -7 -> "${abs(diff)} days ago"
            else -> diff.toString()
        }
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val data = dataSet[position].data.tryJsonCast<Assignment>()!!

        val diff = getDayDifference(data.due_date)

        viewHolder.title.text = data.title
        viewHolder.time.text = getHumanReadableTimeDifference(dataSet[position].createdAt)

        if(diff != previousDiff) {
            viewHolder.timeDiff.text = getReadableFormat(diff)
            previousDiff = diff
        } else {
            viewHolder.timeDiff.visibility = View.GONE
        }
    }

    private fun getReadableFormat(diff: Int): CharSequence {
        return when(diff) {
            0 -> "Today"
            1 -> "Tomorrow"
            else -> "This week"
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

}