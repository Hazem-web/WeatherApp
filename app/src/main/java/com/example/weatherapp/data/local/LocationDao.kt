package com.example.weatherapp.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.weatherapp.data.models.LocationInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLocation(location: LocationInfo):Long

    @Delete
    suspend fun deleteLocation(location: LocationInfo):Int


    @Query("Select * from locations")
    fun getAllLocations():Flow<List<LocationInfo>>
}