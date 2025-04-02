package com.example.weatherapp

enum class Constants(val value: String) {
    NO_ITEM("no item"),
    DELETED("Deleted"),
    NOT_REC("no rec"),
    DONE("done"),
    LOADING("Loading"),
    COLORS("colors"),
    DEGREES("degrees"),
    MODE("mode"),
    SPEED("speed"),
    LANG("lang"),
    DARK("dark"),
    LIGHT("light"),
    LAT("lat"),
    LAN("lan"),
    MAP("map"),
    LOCATION("location"),
    ARABIC("ar"),
    ENGLISH("en")

}

enum class Values(val value: Int){
    MILES(1),
    METER(0),
    KELVIN(1),
    CELSIUS(0),
    FAHRENHEIT(2)
}