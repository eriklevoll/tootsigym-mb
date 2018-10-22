package com.tlab.erik_spectre.tootsigymmb.Model

import java.io.Serializable


//@JsonSerializable
data class Route(val Data: RouteData) : Serializable

//@JsonSerializable
class RouteData(val Moves: List<Move>, val Setter: SetterData) : Serializable {
    var Name = ""
    var Repeats = 0
    var Grade = ""
    var Rating = 0
    var UserRating = 0
    var IsBenchmark = false
}
//@JsonSerializable
class SetterData(val Firstname: String, val Lastname: String) : Serializable

//@JsonSerializable
class Move(val Description: String, val IsStart: Boolean, val IsEnd: Boolean) : Serializable