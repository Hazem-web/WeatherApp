package com.example.weatherapp.data.models

import com.google.gson.annotations.SerializedName

data class WeatherDetails(
    val temp: Double,
    @SerializedName("feels_like") val feels: Double,
    @SerializedName("temp_min") val min: Double,
    @SerializedName("temp_max") val max: Double,
    val pressure: Int,
    val humidity: Int,
    @SerializedName("sea_level") val seaLevel: Int?,
    @SerializedName("grnd_level") val groundLevel: Int?
)