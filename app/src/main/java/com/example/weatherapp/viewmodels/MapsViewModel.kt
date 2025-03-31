package com.example.weatherapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.data.models.ForecastWeatherResponse
import com.example.weatherapp.data.models.GeocodingResponse
import com.example.weatherapp.data.models.Results
import com.example.weatherapp.data.models.WeatherResponse
import com.example.weatherapp.data.repo.WeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MapsViewModel(private val repository: WeatherRepository):ViewModel() {
    private val mutableWeather: MutableStateFlow<Results<WeatherResponse>> = MutableStateFlow(Results.Loading)
    val weather: StateFlow<Results<WeatherResponse>> = mutableWeather

    private val mutablePlaces: MutableStateFlow<Results<List<GeocodingResponse>>> = MutableStateFlow(Results.Loading)
    val places: StateFlow<Results<List<GeocodingResponse>>> = mutablePlaces

    fun getLocation(lat:Double, lon:Double, language: String){
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
        }
    }

    fun getPlaces(query:String){
        if (query.isNotBlank()){
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val places = repository.getCities(query)
                    if (places.isSuccessful) {
                        mutablePlaces.value = Results.Success(places.body())
                    } else {
                        mutablePlaces.value = Results.Failure(
                            Exception(
                                places.errorBody()?.string() ?: "Unknown error"
                            )
                        )
                    }
                }catch (ex:Exception){
                    mutablePlaces.value=Results.Failure(ex)
                }
            }
        }
    }
}


class MapsViewModelFactory(private val repository: WeatherRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MapsViewModel(repository) as T
    }
}