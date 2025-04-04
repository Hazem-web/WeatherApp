package com.example.weatherapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.data.models.ForecastWeatherResponse
import com.example.weatherapp.data.models.GeocodingResponse
import com.example.weatherapp.data.models.LocationInfo
import com.example.weatherapp.data.models.Results
import com.example.weatherapp.data.models.WeatherResponse
import com.example.weatherapp.data.repo.WeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeMapViewModel(private val repository: WeatherRepository):ViewModel() {
    private val mutablePlaces: MutableStateFlow<Results<List<GeocodingResponse>>> = MutableStateFlow(Results.Loading)
    val places: StateFlow<Results<List<GeocodingResponse>>> = mutablePlaces

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



class HomeMapViewModelFactory(private val repository: WeatherRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomeMapViewModel(repository) as T
    }
}