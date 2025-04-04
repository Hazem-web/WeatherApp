package com.example.weatherapp.viewmodels

import androidx.lifecycle.asLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.weatherapp.data.models.LocationInfo
import com.example.weatherapp.data.models.Results
import com.example.weatherapp.data.repo.WeatherRepository
import com.example.weatherapp.getOrAwaitValue
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOf
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasSize
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LocationsViewModelTest{
    private lateinit var locationsViewModel: LocationsViewModel
    private lateinit var repository: WeatherRepository

    @Before
    fun setup(){
        repository= mockk(relaxed = true)
        locationsViewModel=LocationsViewModel(repository)
    }

    @Test
    fun getLocations_returnSuccess()  {
        val mockData = listOf(
            LocationInfo(1.0,1.0,"a","a","a","a"),
            LocationInfo(1.0,1.0,"a","a","a","a")
        )
        coEvery { repository.getAllLocations() } returns flowOf(mockData)

        val liveData = locationsViewModel.locations.filter { it is Results.Success }.asLiveData()
        locationsViewModel.getLocations()

        val result = liveData.getOrAwaitValue()

        assertThat(result, instanceOf(Results.Success::class.java))
        val data = (result as Results.Success).data
        assertThat(data, hasSize(2))
        assertThat(data?.get(0)?.long ?: 0, equalTo(1.0))
    }

    @Test
    fun addLocations_returnDone()  {
        val location = LocationInfo(1.0,1.0,"a","a","a","a")
        coEvery { repository.insertLocation(location) } returns 1L

        val msgLiveData = locationsViewModel.massage.filter { it!="Loading" }.asLiveData()
        locationsViewModel.returnLocation(location)

        val message = msgLiveData.getOrAwaitValue()
        assertThat(message, equalTo("done"))
    }
}