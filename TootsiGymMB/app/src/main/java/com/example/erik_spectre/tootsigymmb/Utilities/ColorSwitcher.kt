package com.example.erik_spectre.tootsigymmb.Utilities

import android.animation.ObjectAnimator
import android.widget.GridLayout

//class ColorSwitcher(private val connectionBar: GridLayout) {
//
//    fun changeBackground(element: GridLayout, fromColor: Int, toColor: Int, duration:Long = 1000) {
//        ObjectAnimator.ofArgb(element, "backgroundColor", fromColor, toColor)
//                .setDuration(duration)
//                .start()
//    }
//
//    fun changeConnectionBarColor(fromColor: Int, toColor: Int) {
//        changeBackground(connectionBar, fromColor, toColor)
//    }
//}

object ColorSwitcher {

    fun changeBackground(element: GridLayout, fromColor: Int, toColor: Int, duration:Long = 1000) {
        ObjectAnimator.ofArgb(element, "backgroundColor", fromColor, toColor)
                .setDuration(duration)
                .start()
    }
}