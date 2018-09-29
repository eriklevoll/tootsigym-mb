package com.tlab.erik_spectre.tootsigymmb.Model

class Person(val id: Long, val name: String, val age: Int = -1)

data class Article(val title: String = "",
                   val body: String = "",
                   val viewCount: Int = 0,
                   val payWall: Boolean = false,
                   val titleImage: String = "")

data class Article2(val id: Int = 0,
                   val text: String = "",
                   val geo: IntArray,
                   val payWall: Boolean = false,
                   val titleImage: String = "")

