package com.example.weatherapp.models

data class PlaceInfo(
    val type: Int?,
    val id: Int?,
    val country: String,
    val sunrise: Long,
    val sunset: Long
)
