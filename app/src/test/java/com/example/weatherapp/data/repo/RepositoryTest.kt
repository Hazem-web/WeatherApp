package com.example.weatherapp.data.repo

import com.example.weatherapp.data.local.LocalDataSource
import com.example.weatherapp.data.models.GeocodingResponse
import com.example.weatherapp.data.models.LocalNames
import com.example.weatherapp.data.models.LocationInfo
import com.example.weatherapp.data.models.Results
import com.example.weatherapp.data.remote.FakeRemoteDataSource
import com.example.weatherapp.data.remote.RemoteDataSource
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual
import org.junit.Before
import org.junit.Test


class RepositoryTest{

    private val localLocations= listOf(
        LocationInfo(0.0,0.0,"a","a","a","a"),
        LocationInfo(1.0,1.0,"b","b","b","b")
    )

    private val onlineCities= listOf(
        GeocodingResponse("cairo",null,0.0,0.0,"egypt"),
        GeocodingResponse("london", LocalNames("london","لندن"),20.0,20.0,"england")
    )
    private lateinit var fakeLocalDataSource: LocalDataSource
    private lateinit var fakeRemoteDataSource: RemoteDataSource
    private lateinit var repository: WeatherRepository

    @Before
    fun setup(){
        fakeRemoteDataSource= FakeRemoteDataSource()
        fakeLocalDataSource= mockk()

        coEvery { fakeLocalDataSource.getAllLocations() } returns flowOf(localLocations)

        repository= WeatherRepositoryImp(remoteDataSource = fakeRemoteDataSource, localDataSource = fakeLocalDataSource)
    }

    @Test
    fun getLocations_returnsLocalLocationsFromLocal() = runTest {
        val tasks = repository.getAllLocations().last()
        assertThat(tasks, IsEqual(localLocations))
    }


    @Test
    fun getCities_returnsRemoteCitiesFromInternet() = runTest {
        val tasks = repository.getCities("e")
        assertThat(tasks.body(), not(nullValue()))
        assertThat(tasks.isSuccessful, `is`(true))
    }
}