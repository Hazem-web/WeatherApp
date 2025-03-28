package com.example.weatherapp.data.remote

import com.example.weatherapp.data.models.ForecastWeatherResponse
import com.example.weatherapp.data.models.WeatherResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {
    @GET("forecast")
    suspend fun getForecast(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("lang") lang:String="en"
    ):Response<ForecastWeatherResponse>

    @GET("weather")
    suspend fun getCurrentWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("lang") lang:String="en"
    ):Response<WeatherResponse>
}