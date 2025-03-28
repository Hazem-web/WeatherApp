package com.example.weatherapp.data.remote

import com.example.weatherapp.data.models.ForecastWeatherResponse
import com.example.weatherapp.data.models.GeocodingResponse
import com.example.weatherapp.data.models.WeatherResponse
import retrofit2.Response


interface RemoteDataSource {
    suspend fun getWeather(
        lat: Double,
        lon: Double,
        lang:String
    ):Response<WeatherResponse>

    suspend fun getForecast(
        lat: Double,
        lon: Double,
        lang:String
    ):Response<ForecastWeatherResponse>

    suspend fun  getCities(place:String):Response<List<GeocodingResponse>>
}