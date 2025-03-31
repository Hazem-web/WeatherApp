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
import com.example.weatherapp.R
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
                    mutableMsg.value = context.getString(R.string.not_found)
                }else {
                    mutableMsg.value= context.getString(R.string.deleted)
                    withContext(Dispatchers.Main){
                        if (setTime>currentTime){
                            cancelNotification(context,notification.id)
                        }
                    }
                }
            }
            catch (ex:Exception){
                mutableMsg.value = ex.message?:context.getString(R.string.not_rec)
            }
        }
    }

    fun addNotification(notification: Notification, context: Context){
        val currentTime=Calendar.getInstance().timeInMillis
        val setTime=notification.date + notification.time

        viewModelScope.launch(Dispatchers.IO){
            try {
                val number=repository.insertNotification(notification)
                if(number.toInt()==1){
                    mutableMsg.value=context.getString(R.string.added)
                    withContext(Dispatchers.Main){
                        if (currentTime<setTime){
                            scheduleEvent( notification = notification, context = context)
                        }
                    }
                }
                else{
                    mutableMsg.value=context.getString(R.string.not_valid)
                }
            } catch (ex:Exception){
                mutableMsg.value=ex.localizedMessage?:context.getString(R.string.not_rec)
            }

        }
    }

    private fun scheduleEvent(context: Context, notification: Notification) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val id=(mutableNotifications.value as Results.Success).data?.last()?.id?:0
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("ID", id+1)
            putExtra("TYPE", notification.type.ordinal)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notification.id,
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
        }
    }

    private fun cancelNotification(context: Context, notificationId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
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