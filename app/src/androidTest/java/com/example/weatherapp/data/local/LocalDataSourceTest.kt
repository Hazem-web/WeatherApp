package com.example.weatherapp.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.weatherapp.data.models.Notification
import com.example.weatherapp.data.models.NotificationType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test


class LocalDataSourceTest{
    lateinit var dao:NotificationDao
    lateinit var database: MyDatabase
    lateinit var localDataSource: LocalDataSource

    @Before
    fun setup(){
        database= Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MyDatabase::class.java
        ).build()
        dao=database.getNotificationsDao()
        localDataSource=FakeLocalDataSource(dao)
    }

    @After
    fun tearDown()= database.close()

    @Test
    fun getNotification_givenFakeNotifications_returnsListSizeOf2() = runTest{
        localDataSource.insertNotification(Notification(time = 10000, date = 1000000, type = NotificationType.NOTIFICATION))
        localDataSource.insertNotification(Notification(time = 100000, date = 10000000, type = NotificationType.NOTIFICATION))

        var result= localDataSource.getAllNotification().first()
        assertThat(result.size, `is`(2))
    }

    @Test
    fun getNotification_addAndDeleteNotifications_returnsListSizeOf3() = runTest{
        localDataSource.insertNotification(Notification(time = 10000, date = 1000000, type = NotificationType.NOTIFICATION))
        val id=localDataSource.insertNotification(Notification(time = 100000, date = 10000000, type = NotificationType.NOTIFICATION))
        localDataSource.insertNotification(Notification(time = 100000, date = 10000000, type = NotificationType.ALARM))
        localDataSource.insertNotification(Notification(time = 1, date = 2, type = NotificationType.ALARM))

        localDataSource.deleteNotification(Notification(id = id.toInt(),time = 100000, date = 10000000, type = NotificationType.NOTIFICATION))

        var result= localDataSource.getAllNotification().first()
        assertThat(result.size, `is`(3))
    }

}