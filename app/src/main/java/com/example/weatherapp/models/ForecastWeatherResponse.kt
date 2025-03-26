package com.example.weatherapp.models

import com.google.gson.annotations.SerializedName

data class ForecastWeatherResponse (
    @SerializedName("cnt") val numberOfListItem: Int,
    @SerializedName("list") val forecastList: List<WeatherResponse>,
    val city: PlaceInfo
)