package com.example.weatherapp.data.remote

import com.example.weatherapp.data.models.GeocodingResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface GeoService {
    @GET("direct")
    suspend fun getCities(
        @Query("q") place:String,
        @Query("limit") limit:Int=5
    ):Response<List<GeocodingResponse>>
}