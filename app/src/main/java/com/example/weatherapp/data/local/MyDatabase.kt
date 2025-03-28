package com.example.weatherapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.weatherapp.data.models.LocationInfo
import com.example.weatherapp.data.models.Notification

@Database(entities = [Notification::class,LocationInfo::class], version = 1, exportSchema = false)
@TypeConverters(NotificationTypeConverter::class)
abstract class MyDatabase:RoomDatabase() {

    abstract fun getNotificationsDao(): NotificationDao

    abstract fun getLocationsDao(): LocationDao

    companion object{
        @Volatile
        private var instance: MyDatabase? = null
        fun getInstance(context: Context): MyDatabase {
            return instance ?: synchronized(this){
                val INSTANCE = Room.databaseBuilder(context, MyDatabase::class.java, "weather_db").build()
                instance = INSTANCE
                INSTANCE
            }
        }
    }
}