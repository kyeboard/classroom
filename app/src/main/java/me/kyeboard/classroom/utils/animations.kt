package me.kyeboard.classroom.utils

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator

fun animationColor(view: View, colorTo: Int, duration: Long = 200) {
    val colorFrom = view.backgroundTintList?.defaultColor ?: Color.TRANSPARENT

    val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
    colorAnimation.duration = duration
    colorAnimation.interpolator = AccelerateDecelerateInterpolator()

    colorAnimation.addUpdateListener { animator ->
        val color = animator.animatedValue as Int
        view.backgroundTintList = ColorStateList.valueOf(color)
    }

    colorAnimation.start()
}


fun animateTintWithBorder(view: View, colorTo: Int, duration: Long = 200) {
    val background = view.background.mutate() as GradientDrawable
    val colorFrom = background.color?.defaultColor ?: Color.TRANSPARENT

    val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
    colorAnimation.duration = duration
    colorAnimation.interpolator = AccelerateDecelerateInterpolator()

    colorAnimation.addUpdateListener { animator ->
        val color = animator.animatedValue as Int

        (view.background.mutate() as GradientDrawable).apply {
            setColor(color)
            setStroke(5, Color.parseColor("#000000"))
            cornerRadius = 10F
        }
    }

    colorAnimation.start()
}