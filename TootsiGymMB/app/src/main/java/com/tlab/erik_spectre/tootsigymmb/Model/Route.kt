package com.tlab.erik_spectre.tootsigymmb.Model

import se.ansman.kotshi.JsonSerializable

@JsonSerializable
class Route(val Data: RouteData)

class RouteData(val Moves: List<Move>, val Setter: SetterData) {
    var Name = ""
    var Repeats = 0
    var Grade = ""
    var Rating = 0
    var UserRating = 0
    var IsBenchmark = false
}

class SetterData(val Firstname: String, val Lastname: String)
class Move(val Description: String, val IsStart: Boolean, val IsEnd: Boolean)
