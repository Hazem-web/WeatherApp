package com.example.weatherapp.viewmodels

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.NotificationReceiver
import com.example.weatherapp.data.models.Notification
import com.example.weatherapp.data.models.Results
import com.example.weatherapp.data.repo.WeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotificationsViewModel(private val repository: WeatherRepository): ViewModel()  {
    private val mutableNotifications: MutableStateFlow<Results<List<Notification>>> = MutableStateFlow(
        Results.Loading)
    val notifications: StateFlow<Results<List<Notification>>> = mutableNotifications

    private val mutableMsg: MutableStateFlow<String> = MutableStateFlow("Loading")
    val massage:StateFlow<String> = mutableMsg

    fun getNotifications(){
        viewModelScope.launch(Dispatchers.IO) {
            val notifications=repository.getAllNotification()
            notifications.catch {
                mutableNotifications.value=Results.Failure(it)
            }.collect{
                mutableNotifications.value=Results.Success(it)
            }
        }
    }

    fun deleteNotification(notification: Notification, context: Context){
        val currentTime=Calendar.getInstance().timeInMillis
        val setTime=notification.date + notification.time
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val number = repository.deleteNotification(notification)
                if (number == 0) {
                    mutableMsg.value = "no item"
                }else {
                    mutableMsg.value= "Deleted"
                    withContext(Dispatchers.Main){
                        if (setTime>currentTime){
                            cancelNotification(context,notification)
                        }
                    }
                }
            }
            catch (ex:Exception){
                mutableMsg.value = ex.localizedMessage?:"no rec"
            }
        }
    }

    fun addNotification(notification: Notification?, context: Context){
        if (notification!=null) {
            val currentTime = Calendar.getInstance().timeInMillis
            val setTime = notification.date + notification.time
            notification.id=0
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val number = repository.insertNotification(notification)
                    if (number.toInt() >= 1) {
                        mutableMsg.value= "done"
                        if (currentTime < setTime) {
                        withContext(Dispatchers.Main) {
                                scheduleEvent(notification = notification, context = context, id = number.toInt())
                            }
                        }
                    } else {
                        mutableMsg.value = "not rec"
                    }
                } catch (ex: Exception) {
                    mutableMsg.value = ex.localizedMessage?:"no rec"
                }

            }
        }
        else{
            mutableMsg.value = "no item"
        }
    }

    private fun scheduleEvent(context: Context, notification: Notification, id:Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("ID", id)
            putExtra("TYPE", notification.type.ordinal)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = notification.date + notification.time

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms())
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        }
        else{
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }

    }

    private fun cancelNotification(context: Context, notification: Notification) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("ID", notification.id)
            putExtra("TYPE", notification.type.ordinal)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
    }

}

class NotificationsViewModelFactory(private val repository: WeatherRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return NotificationsViewModel(repository) as T
    }
}