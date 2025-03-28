package com.example.weatherapp.data.local

import android.content.Context
import com.example.weatherapp.data.models.LocationInfo
import com.example.weatherapp.data.models.Notification
import kotlinx.coroutines.flow.Flow

class LocalDataSourceImp private constructor(context: Context) : LocalDataSource {

    private val locationsDao:LocationDao
    private val notificationDao:NotificationDao

    init {
        val database=MyDatabase.getInstance(context)
        locationsDao=database.getLocationsDao()
        notificationDao=database.getNotificationsDao()
    }

    companion object{
        @Volatile
        private var INSTANCE:LocalDataSourceImp? = null

        fun getInstance(context: Context):LocalDataSourceImp{
            return INSTANCE ?: synchronized(this){
                val instance=LocalDataSourceImp(context)
                INSTANCE=instance
                instance
            }
        }
    }

    override suspend fun insertLocation(location: LocationInfo): Long {
        return locationsDao.insertLocation(location)
    }

    override suspend fun deleteLocation(location: LocationInfo): Int {
        return locationsDao.deleteLocation(location)
    }

    override suspend fun getAllLocations(): Flow<List<LocationInfo>> {
        return locationsDao.getAllLocations()
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