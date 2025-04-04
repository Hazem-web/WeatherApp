package com.example.weatherapp.data.local

import com.example.weatherapp.data.models.LocationInfo
import com.example.weatherapp.data.models.Notification
import kotlinx.coroutines.flow.Flow


class FakeLocalDataSource (private val notificationDao:NotificationDao):LocalDataSource{
    override suspend fun insertLocation(location: LocationInfo): Long {
        TODO("Not yet implemented")
    }

    override suspend fun deleteLocation(location: LocationInfo): Int {
        TODO("Not yet implemented")
    }

    override suspend fun getAllLocations(): Flow<List<LocationInfo>> {
        TODO("Not yet implemented")
    }

    override suspend fun insertNotification(notification: Notification): Long {
        return notificationDao.insertNotification(notification)
    }

    override suspend fun deleteNotification(notification: Notification): Int {
        return notificationDao.deleteNotification(notification)
    }

    override suspend fun getAllNotification(): Flow<List<Notification>> {
        return notificationDao.getAllNotification()
    }

}