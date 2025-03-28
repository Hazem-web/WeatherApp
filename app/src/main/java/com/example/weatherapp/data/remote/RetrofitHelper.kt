package com.example.weatherapp.data.remote

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitHelper {
    private val client = OkHttpClient.Builder()
        .addInterceptor(ApiKeyInterceptor())
        .build()
    private const val BASE_URL = "https://api.openweathermap.org/"
    private val weatherInstance = Retrofit.Builder()
        .baseUrl(BASE_URL+"data/2.5/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()
    private val geoInstance = Retrofit.Builder()
        .baseUrl(BASE_URL+"geo/1.0/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()
    val weatherService = weatherInstance.create(WeatherService::class.java)
    val geoService = geoInstance.create(GeoService::class.java)
}