package com.tlab.erik_spectre.tootsigymmb.Utilities

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
            CanvasData.removeHold(rc)
        } else {
            CanvasData.addHold(rc, "$r,$g,$b")
        }
    }

    private fun parseRouteData(data: String) {
        val routeData = data.split(";")
        for (hold in routeData[0].split(",")) {
            CanvasData.addHold(hold, "0,255,0")
        }
        for (hold in routeData[1].split(",")) {
            CanvasData.addHold(hold, "0,0,255")
        }
        for (hold in routeData[2].split(",")) {
            CanvasData.addHold(hold, "255,0,0")
        }
    }
}