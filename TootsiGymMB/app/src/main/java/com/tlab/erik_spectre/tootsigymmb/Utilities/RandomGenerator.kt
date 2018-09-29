package com.tlab.erik_spectre.tootsigymmb.Utilities

import com.tlab.erik_spectre.tootsigymmb.Model.Route
import java.util.*

object RandomGenerator {
    fun ClosedRange<Int>.random() = Random().nextInt((endInclusive + 1) - start) +  start

    fun getRandomInt(fromVal: Int, toVal: Int): Int {
        return (fromVal..toVal).random()
    }

    fun getRandomID(upperCase: Boolean) : String{
        val randomUUID = java.util.UUID.randomUUID().toString()
        val snippet = randomUUID.split('-')[0]
        return if (upperCase)
            snippet.toUpperCase()
        else
            snippet
    }

    fun getRandomLED() : String {
        val r = getRandomInt(0, 255)
        val g = getRandomInt(0, 255)
        val b = getRandomInt(0, 255)
        return "$r,$g,$b"
    }

    fun getRandomHoldRC() : String {
        val r = Random()
        val i = r.nextInt(11) + 'a'.toInt()
        val letter = i.toChar().toUpperCase()
        val number = getRandomInt(1, 18)
        return "$letter$number"
    }

    fun getRandomRoute() : String{

        fun getHolds(row: Int, to: Int): String {
            val r = Random()
            var route = ""
            for (i in 0..getRandomInt(1, to-1)) {
                val j = r.nextInt(11) + 'a'.toInt()
                val letter = j.toChar().toUpperCase()

                val skip = getRandomInt(0,1)
                if (skip == 0) route += "$letter$row,"
                //route += "$letter${i+row},"
            }
            return route
        }
        val r = Random()
        var route = ""
        var row = 1
        var startHolds = ""
        var startHoldsCount = 0
        while (startHoldsCount == 0) {
            startHolds = getHolds(1, 3)
            startHoldsCount = startHolds.split(",").count()
        }
        row = startHoldsCount
        //val startHolds = getHolds(1, 3)
        //row = startHolds.split(",").count()
        //if ()
        //Starting holds
//        for (i in 1..getRandomInt(1,3)) {
//            val j = r.nextInt(11) + 'a'.toInt()
//            val letter = j.toChar().toUpperCase()
//
//            val skip = getRandomInt(0,1)
//            if (skip == 0) route += "$letter$row,"
//            row++
//        }
        route = startHolds.trimEnd(',')
        route += ";"
        //Main route holds
        for (i in 1..getRandomInt(13,14)) {
            val j = r.nextInt(11) + 'a'.toInt()
            val letter = j.toChar().toUpperCase()

            val skip = getRandomInt(0,1)
            if (skip == 0) route += "$letter$row,"
            row++
        }
        route = route.trimEnd(',')
        route += ";"
        //Top holds
        while (row <= NUM_OF_ROWS) {
            val j = r.nextInt(11) + 'a'.toInt()
            val letter = j.toChar().toUpperCase()

            val skip = getRandomInt(0,1)
            if (skip == 0) route += "$letter$row,"
            row++
        }
        route = route.trimEnd(',')

        return route
    }

    fun getRandomGrade(): String {
        return "V${getRandomInt(1,14)}"
    }

    fun getRandomFromDB(): Route? {
        val grade = getRandomGrade()
        val routes = DataParser.getFilteredRoutes(grade)
        val index = routes?.size?.let { getRandomInt(0, it-1) } ?: 0
        return routes?.get(index)
    }
}