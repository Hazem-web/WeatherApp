package com.example.weatherapp.data.repo

import com.example.weatherapp.data.local.LocalDataSource
import com.example.weatherapp.data.models.ForecastWeatherResponse
import com.example.weatherapp.data.models.GeocodingResponse
import com.example.weatherapp.data.models.LocationInfo
import com.example.weatherapp.data.models.Notification
import com.example.weatherapp.data.models.WeatherResponse
import com.example.weatherapp.data.remote.RemoteDataSource
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

class WeatherRepositoryImp(
    private val localDataSource: LocalDataSource,
    private val remoteDataSource: RemoteDataSource
): WeatherRepository {



    override suspend fun insertLocation(location: LocationInfo): Long {
        return localDataSource.insertLocation(location)
    }

    override suspend fun deleteLocation(location: LocationInfo): Int {
        return localDataSource.deleteLocation(location)
    }

    override suspend fun getAllLocations(): Flow<List<LocationInfo>> {
        return localDataSource.getAllLocations()
    }

    override suspend fun insertNotification(notification: Notification): Long {
        return localDataSource.insertNotification(notification)
    }

    override suspend fun deleteNotification(notification: Notification): Int {
        return localDataSource.deleteNotification(notification)
    }

    override suspend fun getAllNotification(): Flow<List<Notification>> {
        return localDataSource.getAllNotification()
    }

    override suspend fun getWeather(
        lat: Double,
        lon: Double,
        lang: String
    ): Response<WeatherResponse> {
        return remoteDataSource.getWeather(lat,lon,lang)
    }

    override suspend fun getForecast(
        lat: Double,
        lon: Double,
        lang: String
    ): Response<ForecastWeatherResponse> {
        return remoteDataSource.getForecast(lat,lon,lang)
    }

    override suspend fun getCities(place: String): Response<List<GeocodingResponse>> {
        return remoteDataSource.getCities(place)
    }
}