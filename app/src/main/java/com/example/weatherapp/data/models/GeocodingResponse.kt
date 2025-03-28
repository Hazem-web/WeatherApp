package com.example.weatherapp.data.models

import com.google.gson.annotations.SerializedName

data class GeocodingResponse (
    val name: String,
    @SerializedName("local_names") val localNames: LocalNames,
    @SerializedName("lat") val latitude: Double,
    @SerializedName("lon") val longitude: Double,
    val country: String
)