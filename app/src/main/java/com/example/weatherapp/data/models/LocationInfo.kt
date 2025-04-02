package com.example.weatherapp.data.models

import androidx.room.Entity

@Entity(tableName = "locations", primaryKeys = ["city", "country"])
data class LocationInfo(
    val long: Double,
    val lat: Double,
    val city:String,
    val cityAr:String,
    val country:String,
    val countryCode: String
)