package me.kyeboard.classroom.utils

import android.app.Activity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso

fun setText(view: Activity, selector: Int, text: String) {
    view.findViewById<TextView>(selector).text = text
}

fun imageInto(view: Activity, url: String, target: Int) {
    Picasso
        .get()
        .load(url)
        .into(view.findViewById<ImageView>(target))
}

fun imageIntoWidget(widget: ImageView, url: String) {
    Picasso
        .get()
        .load(url)
        .into(widget)
}


fun invisible(target: View) {
    target.visibility = View.GONE
}

fun visible(target: View) {
    target.visibility = View.VISIBLE
}