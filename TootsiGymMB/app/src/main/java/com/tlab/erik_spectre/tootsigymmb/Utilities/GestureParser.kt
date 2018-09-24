package com.tlab.erik_spectre.tootsigymmb.Utilities

object GestureParser {

    var height = 0;
    var width = 0;
    private var topbarHeight = 0

    fun setScreenDimensions(h: Int, w: Int) {
        height = h
        width = w
    }


    fun onDown(X: Float?, Y: Float?, t: Int): String {
        topbarHeight = t
        if (X == null) return ""
        if (Y == null) return ""

        return convertCoordToRC(X, Y - topbarHeight)
    }

    fun convertCoordToRC(x: Float, y: Float) : String {
        val relativeX = x / width
        val relativeY = y / (height - topbarHeight)
        val row = Math.round(-19.8596026 * relativeY + 19.75629139)
        val col = Math.round(13.1291028 * relativeX - 1.03501094)
        val cCol = (col+64).toChar()

        return "$cCol$row"
    }
}