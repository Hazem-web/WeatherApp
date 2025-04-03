package com.example.weatherapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.weatherapp.data.models.NotificationType

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val id = intent?.getIntExtra("ID", 0)?: 0
        val type = intent?.getIntExtra("TYPE", 0)?:0

        when (type) {
            NotificationType.NOTIFICATION.ordinal -> showNotification(context, id)
            NotificationType.ALARM.ordinal -> showAlarmPopup(context, id)
        }
    }

    private fun showAlarmPopup(context: Context, id: Int) {
        val channelId = "alarm_channel"
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()
            val channel = NotificationChannel(
                channelId, "Alarms", NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setSound(Uri.parse("android.resource://" + context.packageName + "/" + R.raw.alarm),audioAttributes)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Alarm")
            .setContentText("Your scheduled Alarm is now!")
            .setSmallIcon(R.mipmap.ic_launcher) // Replace with your icon
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(Uri.parse("android.resource://${context.packageName}/${R.raw.alarm}"))
            .setAutoCancel(true)
            .build()

        notificationManager.notify(id, notification)
    }

    private fun showNotification(context: Context, id: Int) {
        val channelId = "notification_channel"
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "General Notifications", NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Notification")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentText("Your scheduled Notification is now!")
            .build()

        notificationManager.notify(id, notification)
    }

}