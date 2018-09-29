package com.tlab.erik_spectre.tootsigymmb.Model

import se.ansman.kotshi.JsonSerializable


@JsonSerializable
class Person(val id: Long, val age: Int = -1)

@JsonSerializable
class Route(val Data: List<RouteData>, val Total: Int)

class RouteData(val Problem: ProblemData) {
    var Id = 0
    var Attempts = 0
    var Grade = ""
    var NumberOfTries = ""
    var Rating = 0
    var IsSuggestedBenchmark = false
    var User = UserData()
}

class ProblemData(val Moves: List<Move>) {
    var Grade = ""
    var Name = ""
}

class UserData()

class Move(val Description: String, val IsStart: Boolean, val IsEnd: Boolean)
