package com.tlab.erik_spectre.tootsigymmb.Utilities

import android.app.Application
import android.app.PendingIntent.getActivity
import android.util.JsonReader
import com.google.gson.Gson
import com.tlab.erik_spectre.tootsigymmb.Model.Article
import com.tlab.erik_spectre.tootsigymmb.Model.Article2
import com.tlab.erik_spectre.tootsigymmb.Model.Person
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.Charset

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

    fun loadJSONFromAsset(app: Application): String {
        val file_name = "routes.json"
        val json_string = app.assets.open(file_name).bufferedReader().use {
            it.readText()
        }
        return json_string
    }

    fun setupJsonParser(app: Application) {

    }

    fun parseJson (app: Application) {
        val json = """
   { "title": "Most elegant way of using Gson + Kotlin with default values and null safety",
     "body": null,
     "viewCount": 9999,
     "payWall": false,
     "ignoredProperty": "Ignored"
   }
"""
        val json2 = """
           [
  {
    "id": 912345678901,
    "text": "How do I read JSON on Android?",
    "geo": null,
    "user": {
      "name": "android_newb",
      "followers_count": 41
    }
  },
  {
    "id": 912345678902,
    "text": "@android_newb just use android.util.JsonReader!",
    "geo": [50.454722, -104.606667],
    "user": {
      "name": "jesse",
      "followers_count": 2
    }
  }
]
        """

        val article = Gson().fromJson(json, Article2::class.java)
        println(article)


        return
        //val result = mutableListOf<Person>()
        val iss = app.assets.open("test.json")
        val reader = JsonReader(InputStreamReader(iss, "UTF-8"))
        reader.beginArray()
        reader.beginObject()
        try {
            while (reader.hasNext()) {
                println(reader.nextName())
                reader.skipValue()
            }
        } catch (e: java.lang.Exception) {
            println("Exception: ${e.message}")
        }
        reader.endObject()
        reader.endArray()
    }

    fun parse(reader: JsonReader): List<Person> {
        val result = mutableListOf<Person>()

        reader.beginArray()
        while (reader.hasNext()) {
            var id: Long = -1L
            var name: String = ""
            var age: Int = -1

            reader.beginObject()
            while (reader.hasNext()) {
                when (reader.nextName()) {
                    "id" -> id = reader.nextLong()
                    "name" -> name = reader.nextString()
                    "age" -> age = reader.nextInt()
                    else -> reader.skipValue()
                }
            }
            reader.endObject()

            if (id == -1L || name == "") {
                println("Error: Missing required field")
            }
            val person = Person(id, name, age)
            result.add(person)
        }
        reader.endArray()

        return result
    }
}