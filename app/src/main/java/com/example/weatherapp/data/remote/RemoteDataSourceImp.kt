package com.example.weatherapp.data.remote

import com.example.weatherapp.data.models.ForecastWeatherResponse
import com.example.weatherapp.data.models.GeocodingResponse
import com.example.weatherapp.data.models.WeatherResponse
import retrofit2.Response

class RemoteDataSourceImp private constructor():RemoteDataSource{

    companion object{
        private var INSTANCE:RemoteDataSourceImp?=null

        fun getInstance():RemoteDataSourceImp{
            return INSTANCE ?: synchronized(this){
                val instance=RemoteDataSourceImp()
                INSTANCE=instance
                instance
            }
        }
    }

    override suspend fun getWeather(
        lat: Double,
        lon: Double,
        lang: String
    ): Response<WeatherResponse> {
        return RetrofitHelper.weatherService.getCurrentWeather(lat,lon,lang)
    }

    override suspend fun getForecast(
        lat: Double,
        lon: Double,
        lang: String
    ): Response<ForecastWeatherResponse> {
        return RetrofitHelper.weatherService.getForecast(lat,lon,lang)
    }

    override suspend fun getCities(place: String): Response<List<GeocodingResponse>> {
        return RetrofitHelper.geoService.getCities(place)
    }


}