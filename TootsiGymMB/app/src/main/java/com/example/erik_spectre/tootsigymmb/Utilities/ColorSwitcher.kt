package com.example.erik_spectre.tootsigymmb.Utilities

import android.animation.ObjectAnimator
import android.widget.GridLayout

object ColorSwitcher {

    fun changeBackground(element: GridLayout, fromColor: Int, toColor: Int, duration:Long = 1000) {
        ObjectAnimator.ofArgb(element, "backgroundColor", fromColor, toColor)
                .setDuration(duration)
                .start()
    }
}