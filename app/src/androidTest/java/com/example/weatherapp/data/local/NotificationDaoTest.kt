package com.example.weatherapp.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.example.weatherapp.data.models.Notification
import com.example.weatherapp.data.models.NotificationType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class NotificationDaoTest {
    lateinit var dao:NotificationDao
    lateinit var database: MyDatabase

    @Before
    fun setup(){
        database= Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MyDatabase::class.java
        ).build()

        dao=database.getNotificationsDao()
    }

    @After
    fun tearDown()= database.close()

    @Test
    fun getIdOfNotification_givenFakeNotification_returnsTheIdOf1() = runTest{
        val result=dao.insertNotification(Notification(time = 100000, date = 10000000, type = NotificationType.NOTIFICATION))

        assertThat(result, `is`(1))
    }

    @Test
    fun deleteNotification_givenFakeNotifications_returnsListSizeOf1() = runTest{
        dao.insertNotification(Notification(time = 10000, date = 1000000, type = NotificationType.NOTIFICATION))
        dao.insertNotification(Notification(time = 100000, date = 10000000, type = NotificationType.NOTIFICATION))

        dao.deleteNotification(Notification(id = 1,time = 100000, date = 10000000, type = NotificationType.NOTIFICATION))

        var result= dao.getAllNotification().first()

        assertThat(result.size, `is`(1))
    }
}