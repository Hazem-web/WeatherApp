package com.example.weatherapp.data.local

import com.example.weatherapp.data.models.LocationInfo
import com.example.weatherapp.data.models.Notification
import kotlinx.coroutines.flow.Flow

interface LocalDataSource {
    suspend fun insertLocation(location: LocationInfo):Long

    suspend fun deleteLocation(location: LocationInfo):Int

    suspend fun getAllLocations(): Flow<List<LocationInfo>>

    suspend fun insertNotification(notification: Notification):Long

    suspend fun deleteNotification(notification: Notification):Int

    suspend fun getAllNotification(): Flow<List<Notification>>
}