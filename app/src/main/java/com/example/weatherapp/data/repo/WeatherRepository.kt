package com.example.weatherapp.data.repo

import com.example.weatherapp.data.models.ForecastWeatherResponse
import com.example.weatherapp.data.models.GeocodingResponse
import com.example.weatherapp.data.models.LocationInfo
import com.example.weatherapp.data.models.Notification
import com.example.weatherapp.data.models.WeatherResponse
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

interface WeatherRepository {
    suspend fun insertLocation(location: LocationInfo):Long

    suspend fun deleteLocation(location: LocationInfo):Int

    suspend fun getAllLocations(): Flow<List<LocationInfo>>

    suspend fun insertNotification(notification: Notification):Long

    suspend fun deleteNotification(notification: Notification):Int

    suspend fun getAllNotification(): Flow<List<Notification>>

    suspend fun getWeather(
        lat: Double,
        lon: Double,
        lang:String
    ): Response<WeatherResponse>

    suspend fun getForecast(
        lat: Double,
        lon: Double,
        lang:String
    ): Response<ForecastWeatherResponse>

    suspend fun getCities(place:String): Response<List<GeocodingResponse>>
}