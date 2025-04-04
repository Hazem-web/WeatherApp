package com.example.weatherapp.data.remote

import com.example.weatherapp.data.models.ForecastWeatherResponse
import com.example.weatherapp.data.models.GeocodingResponse
import com.example.weatherapp.data.models.WeatherResponse
import retrofit2.Response


class FakeRemoteDataSource:RemoteDataSource{
    override suspend fun getWeather(
        lat: Double,
        lon: Double,
        lang: String
    ): Response<WeatherResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun getForecast(
        lat: Double,
        lon: Double,
        lang: String
    ): Response<ForecastWeatherResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun getCities(place: String): Response<List<GeocodingResponse>> {
        return RetrofitHelper.geoService.getCities(place)
    }

}