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
        val letter = getRandomColumn()
        val number = getRandomInt(1, 18)
        return "$letter$number"
    }

    fun getRandomColumn(): Char {
        val r = Random()
        val i = r.nextInt(11) + 'a'.toInt()
        return i.toChar().toUpperCase()
    }

    fun getRandomRows() : List<Int> {
        val num = (2..17).random()
        val shuffledList  = (1..18).toList().shuffled()
        return shuffledList.subList(0,num).sorted()
    }

    fun getRandomRoute() : String {
        val rows = getRandomRows().toMutableList()
        if (rows.size < 3) return getRandomRoute()

        val startHolds: List<Int>
        val midHolds: List<Int>
        val endHolds: List<Int>

        when {
            rows.size == 3 -> {
                startHolds = rows.subList(0,1)
                midHolds = rows.subList(1,2)
                endHolds = rows.subList(2,3)
            }
            rows.size == 4 -> {
                startHolds = rows.subList(0,1)
                midHolds = rows.subList(1,3)
                endHolds = rows.subList(3,4)
            }
            rows.size == 5 -> {
                startHolds = rows.subList(0,1)
                midHolds = rows.subList(1,4)
                endHolds = rows.subList(4,5)
            }
            else -> {
                val firstStartHold = getRandomInt(0,2)
                val lastStartHold = getRandomInt(firstStartHold+1,3)
                startHolds = rows.subList(firstStartHold, lastStartHold)

                val firstEndHold = rows.size - getRandomInt(1,2)
                val lastEndHold = getRandomInt(firstEndHold+1, rows.size)
                if (firstEndHold <= lastStartHold) return getRandomRoute()

                endHolds = rows.subList(firstEndHold, lastEndHold)

                val firstMidHold = getRandomInt(lastStartHold+1, firstEndHold)
                val lastMidHold = getRandomInt(firstMidHold, firstEndHold)
                midHolds = rows.subList(firstMidHold, lastMidHold)
            }
        }

        if (midHolds.isEmpty()) return getRandomRoute()
        if (endHolds.max()!! < 16) return getRandomRoute()
        if (startHolds.max()!! > 6) return getRandomRoute()

        var startHoldsString = ""
        for (row in startHolds) {
            val letter = getRandomColumn()
            startHoldsString += "$letter$row,"
        }
        startHoldsString = startHoldsString.trimEnd(',')

        var midHoldsString = ""
        for (row in midHolds) {
            val letter = getRandomColumn()
            midHoldsString += "$letter$row,"
        }
        midHoldsString = midHoldsString.trimEnd(',')

        var endHoldsString = ""
        for (row in endHolds) {
            val letter = getRandomColumn()
            endHoldsString += "$letter$row,"
        }
        endHoldsString = endHoldsString.trimEnd(',')

        return "$startHoldsString;$midHoldsString;$endHoldsString"
    }

    fun getRandomGrade(): String {
        return "V${getRandomInt(1,14)}"
    }

    fun getRandomFromDB(routeList: List<Route>?): Route? {

//        val max = RoutesData.data2?.count() ?: 1
//        val index = getRandomInt(0, max-1)

        //val fontGrades = GradeMapping[vGrade]

        //val newList = RoutesData.data2?.filter { RoutesData.applyRoutesFilter(it) }

        if (routeList?.count() == 0) return null

        val max = routeList?.count() ?: 1
        val index = getRandomInt(0, max-1)

        return routeList?.get(index)

        //val grade = getRandomGrade()
//
//        val routes1 = DataParser.getFilteredRoutes(fontGrades?.elementAt(0))
//
//        val joinedList: List<Route>?
//        if (fontGrades?.count() == 1) {
//            joinedList = routes1
//        } else if (fontGrades?.count() == 2) {
//            val routes2 = DataParser.getFilteredRoutes(fontGrades.elementAt(1))
//            if (routes1 == null || routes2 == null) return null
//            joinedList = routes1 + routes2
//        } else {
//            return null
//        }
//
//        if (joinedList == null) return null
//        if (joinedList.isEmpty()) return null
//
//        val index = getRandomInt(0, joinedList.size -1)
//        return joinedList[index]
    }
}