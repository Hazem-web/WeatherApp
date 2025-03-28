package com.example.weatherapp.data.models

import com.google.gson.annotations.SerializedName

data class LocalNames(
    @SerializedName("en") val english: String?,
    @SerializedName("ar") val arabic: String?
)