package com.tlab.erik_spectre.tootsigymmb.Utilities

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.os.AsyncTask
import android.widget.ImageView
import com.squareup.moshi.JsonReader
import com.tlab.erik_spectre.tootsigymmb.Model.Route
import com.tlab.erik_spectre.tootsigymmb.Utilities.GestureParser.height
import java.io.BufferedReader
import java.io.*
//import java.io.InputStreamReader
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL


class ImageDownloader: AsyncTask<String, Void, String>() {
    override fun doInBackground(vararg p0: String?): String {
        return try {
            val url = URL("https://s3.eu-west-2.amazonaws.com/j-bucket0092/mb/data.json")
            val connection: HttpURLConnection = url.openConnection() as HttpURLConnection

            connection.connect()
            val stream = connection.inputStream

            val inputStreamReader = InputStreamReader(stream)
            val bufferedReader = BufferedReader(inputStreamReader)

            bufferedReader.readText()

        } catch (e: Exception) {
            println("Failed: ${e.message}")
            ""
        }
    }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
    }
}

class initJson: AsyncTask<Context, Void, String>() {
    override fun doInBackground(vararg p0: Context): String {
        val inputStream = p0[0].openFileInput("routes9.json")
        val inputString = inputStream.bufferedReader().use { it.readText() }
        inputStream.close()
        RoutesData.initString(inputString)
        return inputString
    }

    override fun onPostExecute(result: String) {
        super.onPostExecute(result)
    }
}

@SuppressLint("StaticFieldLeak")
object HoldsCanvas {
    var canvasPaint = Paint()
    var mainCanvas = Canvas()

    lateinit var canvasView: ImageView
    var holdsData: HashMap<String, Int> = hashMapOf()

    fun addHold(hold: String, color: Int) {
        holdsData[hold] = color
        HoldsCanvas.updateCanvas()
    }

    fun addHold(hold: String, color: String, update: Boolean = true) {
        val c = HoldsCanvas.convertColorString(color)
        if (holdsData[hold] == c) return
        holdsData[hold] = c
        if (update) HoldsCanvas.updateCanvas()
    }

    fun removeHold(hold: String) {
        if (holdsData[hold] == Color.TRANSPARENT)  return
        holdsData.remove(hold)
        HoldsCanvas.updateCanvas()
    }
    fun init(screenSize: IntArray) {
        height = screenSize[0]
        val width = screenSize[1]
        val top = screenSize[2]

        canvasPaint = Paint()
        canvasPaint.isAntiAlias = true
        canvasPaint.style = Paint.Style.STROKE
        canvasPaint.strokeWidth = STROKE_MULTIPLIER * height

        val bitmap = Bitmap.createBitmap(width, height-top, Bitmap.Config.ARGB_8888)
        mainCanvas = Canvas(bitmap)
        canvasView.setImageBitmap(bitmap)
    }
    fun setCanvasImage(image: ImageView) {
        canvasView = image
    }

    fun drawCircle(x: Float, y: Float, color: Int, updating: Boolean = false) {
        canvasPaint.color = color
        mainCanvas.drawCircle(x, y, RADIUS_MULTIPLIER * height, canvasPaint)
        if (!updating) canvasView.invalidate()
    }

    fun drawHoldCircle(hold: String, color: Int, updating: Boolean = false) {
        val coords = GestureParser.convertRCtoCoord(hold)
        val x = coords[0]
        val y = coords[1]

        drawCircle(x, y, color, updating)
    }

    fun drawHoldCircle(hold: String, c: String, updating: Boolean = false) {
        val coords = GestureParser.convertRCtoCoord(hold)
        val x = coords[0]
        val y = coords[1]

        val color = convertColorString(c)

        drawCircle(x, y, color, updating)
    }

    fun convertColorString(c: String): Int {
        return when (c) {
            NO_COLOR -> Color.TRANSPARENT
            RED_COLOR -> Color.RED
            BLUE_COLOR -> Color.BLUE
            GREEN_COLOR -> Color.GREEN
            else -> Color.BLUE
        }
    }

    fun addRouteFromDB(route: Route?): String {
        var startHolds = ""
        var midHolds = ""
        var endHolds = ""

        val moves = route?.Data?.Moves
        if (moves != null) {
            for (move in moves) {
                val rc = move.Description
                val color = DataParser.convertHoldTypeToColor(move.IsStart, move.IsEnd)
                addHold(rc, color, false)

                when {
                    move.IsStart -> startHolds += "$rc,"
                    move.IsEnd -> endHolds += "$rc,"
                    else -> midHolds += "$rc,"
                }
            }
        }

        startHolds = startHolds.trimEnd(',')
        midHolds = midHolds.trimEnd(',')
        endHolds = endHolds.trimEnd(',')

        HoldsCanvas.updateCanvas()
        return "$startHolds;$midHolds;$endHolds"
    }

    fun updateCanvas() {
        clear(false)

        for ((hold, color) in holdsData) {
            drawHoldCircle(hold, color, true)
        }
        canvasView.invalidate()
    }

    fun clear(clearData: Boolean = true) {
        if (clearData) holdsData.clear()
        mainCanvas.drawColor(0, PorterDuff.Mode.CLEAR)
        canvasView.invalidate()
    }
}