import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
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
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.abs

data class Assignment(val author: String, val title: String, val due_date: String, val `$id`: String)

val isoDateFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
val dateFormat = SimpleDateFormat("d MMMM, yyyy", Locale.ENGLISH)

class AssignmentAdapter(private val dataSet: List<Document>, private val onClick: (id: String) -> Unit) :
    RecyclerView.Adapter<AssignmentAdapter.ViewHolder>() {

    val today = Date()
    var previous_title = ""

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView
        val time: TextView
        val parent: ConstraintLayout
        val timeDiff: TextView

        init {
            title = view.findViewById(R.id.class_assignment_item_title)
            parent = view.findViewById(R.id.assignment_item_parent)
            time = view.findViewById(R.id.class_assignment_item_time)
            timeDiff = view.findViewById(R.id.class_assignments_time_header)
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.class_assignments_item, viewGroup, false)

        return ViewHolder(view)
    }

    fun getDaysDifference(newDate: String): Long {
        val parsedDate = isoDateFormatter.parse(newDate)
        val diff = parsedDate.time - today.time

        return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val data = dataSet[position].data.tryJsonCast<Assignment>()!!

        val days_difference = getDaysDifference(data.due_date)
        val new_title = getTitleForDiff(days_difference)

        viewHolder.parent.setOnClickListener {
            onClick(data.`$id`)
        }

        viewHolder.title.text = data.title
        viewHolder.time.text = dateFormat.format(isoDateFormatter.parse(dataSet[position].createdAt)!!)

        if(previous_title != new_title) {
            previous_title = new_title
            viewHolder.timeDiff.text = new_title
        } else {
            viewHolder.timeDiff.visibility = View.GONE
        }
    }

    private fun getTitleForDiff(difference: Long): String {
        return when {
            difference < 0 -> "Missed"
            difference == 0L -> "Today"
            difference == 1L -> "Tomorrow"
            else -> "Others"
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size
}