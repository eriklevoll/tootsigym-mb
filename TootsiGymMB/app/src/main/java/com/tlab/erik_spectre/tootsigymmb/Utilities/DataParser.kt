package com.tlab.erik_spectre.tootsigymmb.Utilities

import com.tlab.erik_spectre.tootsigymmb.Model.*

object DataParser {

    fun parseMQTTResponseData(data: String) {
        try {
            val vals = data.split(":")[1]
            if (vals.contains(";")) {
                parseRouteData(vals)
            } else {
                parseSingleHoldData(vals)
            }
        } catch (e: Exception) {
            println("Failed to parse response, ${e.message}")
        }
    }

    private fun parseSingleHoldData(data: String) {
        val holdData = data.split(",")
        val rc = holdData[0]
        if (rc == "-1") {
            HoldsCanvas.clear()
            return
        }
        val r = holdData[1]
        val g = holdData[2]
        val b = holdData[3]

        if (r == "0" && g == "0" && b == "0") {
            HoldsCanvas.removeHold(rc)
        } else {
            HoldsCanvas.addHold(rc, "$r,$g,$b")
        }
    }

    private fun parseRouteData(data: String) {
        HoldsCanvas.clear(true)
        val routeData = data.split(";")
        for (hold in routeData[0].split(",")) {
            HoldsCanvas.addHold(hold, "0,255,0", false)
        }
        for (hold in routeData[1].split(",")) {
            HoldsCanvas.addHold(hold, "0,0,255", false)
        }
        for (hold in routeData[2].split(",")) {
            HoldsCanvas.addHold(hold, "255,0,0", false)
        }
        HoldsCanvas.updateCanvas()
    }

    fun getFilteredRoutes(grade: String = "V1"): List<Route>? {
        return RoutesData.data?.get(grade)
    }

    fun convertHoldTypeToColor(isStart: Boolean, isEnd: Boolean) : String{
        return when {
            isStart -> "0,255,0"
            isEnd -> "255,0,0"
            else -> return "0,0,255"
        }
    }
}