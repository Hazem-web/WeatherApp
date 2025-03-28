package com.example.weatherapp.data.local

import androidx.room.TypeConverter
import com.example.weatherapp.data.models.NotificationType

class NotificationTypeConverter {
    @TypeConverter
    fun fromNotificationType(type: NotificationType): Int  {
        return type.ordinal
    }

    @TypeConverter
    fun toNotificationType(value: Int): NotificationType {
        return NotificationType.entries[value] // Convert String back to Enum
    }
}