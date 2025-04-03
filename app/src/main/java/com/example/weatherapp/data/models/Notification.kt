package com.example.weatherapp.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class Notification(
    @PrimaryKey(autoGenerate = true)
    var id:Int= 0,
    val time:Long,
    val date:Long,
    val type: NotificationType
)

enum class NotificationType{
    NOTIFICATION,
    ALARM
}