package com.example.weatherapp.data.models

import kotlinx.serialization.Serializable

@Serializable
data class WeatherDto (
    val lat:Double,
    val lon:Double,
)