package com.tlab.erik_spectre.tootsigymmb.Utilities

//const val MQTT_DEVICE_NAME = "MB_APP"

//const val MOONBOARD_DATA_SERVICE_UUID   = "4880c12c-fdcb-4077-8920-a450d7f9b907"
//const val MOONBOARD_DATA_CHAR_UUID      = "afc13ec4-6d71-4442-9f81-55bc21d658d6"
//const val MOONBOARD_DATA_DESCRIPTOR_UUID      = "aec13ec4-6f71-4442-9f81-55bc21d658d6"

//const val DEVICE_SERVICE_UUID           = "12EFFA29-0B6E-40B3-9181-BE9509B23200"

const val MQTT_SUBSCRIBE_TOPIC = "moon_resp"
const val MQTT_PUBLISH_TOPIC = "moon"

const val RADIUS_MULTIPLIER = 0.017f
const val STROKE_MULTIPLIER = 0.0065f

const val RED_COLOR     = "255,0,0"
const val BLUE_COLOR    = "0,0,255"
const val GREEN_COLOR   = "0,255,0"
const val NO_COLOR      = "0,0,0"

const val NUM_OF_ROWS   = 18
const val NUM_OF_COLS   = 11

const val VIEWMODE_GRID = 0
const val VIEWMODE_DATABASE = 1

const val ROUTES_FILE_NAME = "data_routes.txt"
const val ROUTES_FILE_BASEURL = "https://s3.eu-west-2.amazonaws.com/"
const val ROUTES_FILE_FILEURL = "j-bucket0092/mb/data_50p.json"

val GradeMapping: HashMap<String, HashSet<String>> = hashMapOf(
        "V0" to hashSetOf("4"),
        "V1" to hashSetOf("5"),
        "V2" to hashSetOf("5+"),
        "V3" to hashSetOf("6A", "6A+"),
        "V4" to hashSetOf("6B", "6B+"),
        "V5" to hashSetOf("6C", "6C+"),
        "V6" to hashSetOf("7A"),
        "V7" to hashSetOf("7A+"),
        "V8" to hashSetOf("7B","7B+"),
        "V9" to hashSetOf("7B+", "7C"),
        "V10" to hashSetOf("7C+"),
        "V11" to hashSetOf("8A"),
        "V12" to hashSetOf("8A+"),
        "V13" to hashSetOf("8B"),
        "V14" to hashSetOf("8B+"),
        "V15" to hashSetOf("8C"),
        "V16" to hashSetOf("8C+"),
        "V17" to hashSetOf("9A")
        )