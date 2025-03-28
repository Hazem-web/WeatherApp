package com.example.weatherapp.data.remote

import com.example.weatherapp.data.models.ForecastWeatherResponse
import com.example.weatherapp.data.models.GeocodingResponse
import com.example.weatherapp.data.models.WeatherResponse

sealed class WeatherResult {
    data object Loading : WeatherResult()
    data class Success(val data: WeatherResponse) : WeatherResult()
    data class Failure(val error: Throwable) : WeatherResult()
}

sealed class ForecastResult{
    data object Loading : ForecastResult()
    data class Success(val data: ForecastWeatherResponse) : ForecastResult()
    data class Failure(val error: Throwable) : ForecastResult()
}

sealed class GeocodingResult{
    data object Loading : GeocodingResult()
    data class Success(val data: GeocodingResponse) : GeocodingResult()
    data class Failure(val error: Throwable) : GeocodingResult()
}