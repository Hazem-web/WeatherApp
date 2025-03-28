package com.example.weatherapp.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.weatherapp.data.models.Notification
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNotification(notification: Notification):Long

    @Delete
    suspend fun deleteNotification(notification: Notification):Int


    @Query("Select * from notifications")
    fun getAllNotification(): Flow<List<Notification>>
}