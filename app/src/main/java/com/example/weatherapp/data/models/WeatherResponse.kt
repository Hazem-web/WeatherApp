package com.example.weatherapp.data.models

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    @SerializedName("coord") val coordinates: Coordinates,
    val weather: List<Weather>,
    val base: String,
    @SerializedName("main") val details: WeatherDetails,
    val clouds: Clouds,
    val visibility: Int,
    val wind: Wind,
    val dt: Long,
    @SerializedName("sys") val place: PlaceInfo,
    val timezone: Int,
    val id: Int,
    val name: String,
    val cod: Int
)