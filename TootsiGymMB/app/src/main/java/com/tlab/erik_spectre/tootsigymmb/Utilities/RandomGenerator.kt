package com.tlab.erik_spectre.tootsigymmb.Utilities

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
}