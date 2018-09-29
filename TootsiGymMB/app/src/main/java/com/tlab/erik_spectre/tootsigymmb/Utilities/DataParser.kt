package com.tlab.erik_spectre.tootsigymmb.Utilities

import android.app.Application
import com.squareup.moshi.*
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.tlab.erik_spectre.tootsigymmb.Model.*
import okio.BufferedSource
import org.json.JSONArray
import org.json.JSONObject

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

    fun loadJSONFromAsset(app: Application) {
//        val file_name = "routes.json"
//        val json_string = app.assets.open(file_name).bufferedReader().use {
//            it.readText()
//        }
//        val moshi = Moshi.Builder()
//                .add(KotlinJsonAdapterFactory())
//                .build()
//
//
//        val listType = Types.newParameterizedType(List::class.java, Route::class.java)
//        val adapter: JsonAdapter<List<Route>> = moshi.adapter(listType)
//        val result = adapter.fromJson(json_string)

        val result = RoutesData.data

        println(result)
        println("Total: ${result?.get(0)?.Total}")
        println("Data: ${result?.get(0)?.Data?.get(0)}")
        println("Id: ${result?.get(0)?.Data?.get(0)?.Id}")
        println("Grade: ${result?.get(0)?.Data?.get(0)?.Grade}")
        println("Grade: ${result?.get(0)?.Data?.get(0)?.Problem?.Grade}")
        println("Move1: ${result?.get(0)?.Data?.get(0)?.Problem?.Moves?.get(0)?.Description}")

    }
}