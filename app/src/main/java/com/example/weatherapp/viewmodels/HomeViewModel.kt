package com.example.weatherapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.data.models.ForecastWeatherResponse
import com.example.weatherapp.data.models.Results
import com.example.weatherapp.data.models.WeatherResponse
import com.example.weatherapp.data.repo.WeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: WeatherRepository):ViewModel() {
    private val mutableWeather: MutableStateFlow<Results<WeatherResponse>> = MutableStateFlow(Results.Loading)
    val weather: StateFlow<Results<WeatherResponse>> = mutableWeather

    private val mutableForecast: MutableStateFlow<Results<ForecastWeatherResponse>> = MutableStateFlow(Results.Loading)
    val forecast: StateFlow<Results<ForecastWeatherResponse>> = mutableForecast

    fun getAll( lat:Double, lon:Double, language: String){
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val weather=repository.getWeather(lat,lon,language)
                if (weather.isSuccessful){
                    mutableWeather.value=Results.Success(weather.body())
                }
                else{
                    mutableWeather.value=Results.Failure(Exception(weather.errorBody()?.string() ?: "Unknown error"))
                }
            }catch (ex:Exception){
                mutableWeather.value=Results.Failure(ex)
            }
            try {
                val forecast =repository.getForecast(lat,lon,language)
                if (forecast.isSuccessful){
                    mutableForecast.value=Results.Success(forecast.body())
                }
                else{
                    mutableForecast.value=Results.Failure(Exception(forecast.errorBody()?.string() ?: "Unknown error"))
                }
            }catch (ex:Exception){
                mutableForecast.value=Results.Failure(ex)
            }
        }
    }
}


class HomeViewModelFactory(private val repository: WeatherRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomeViewModel(repository) as T
    }
}