package com.example.weatherapp.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.R
import com.example.weatherapp.data.models.LocationInfo
import com.example.weatherapp.data.models.Results
import com.example.weatherapp.data.repo.WeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class LocationsViewModel(private val repository: WeatherRepository): ViewModel() {
    private val mutableLocations: MutableStateFlow<Results<List<LocationInfo>>> = MutableStateFlow(
        Results.Loading)
    val locations: StateFlow<Results<List<LocationInfo>>> = mutableLocations

    private val mutableMsg: MutableSharedFlow<String> = MutableSharedFlow()
    val massage:SharedFlow<String> = mutableMsg

    fun getLocations(){
        viewModelScope.launch(Dispatchers.IO) {
            val locations=repository.getAllLocations()
            locations.catch {
                mutableLocations.value=Results.Failure(it)
            }.collect{
                mutableLocations.value=Results.Success(it)
            }
        }
    }

    fun deleteLocation(locationInfo: LocationInfo){
        viewModelScope.launch(Dispatchers.IO) {
                try {
                    val number = repository.deleteLocation(locationInfo)
                    if (number == 0) {
                        mutableMsg.emit( "no item")
                    }else {
                        mutableMsg.emit( "Deleted")
                    }
                }
                catch (ex:Exception){
                    mutableMsg.emit(ex.localizedMessage?:"no rec")
                }
            }


    }

    fun returnLocation(locationInfo: LocationInfo?){
        viewModelScope.launch(Dispatchers.IO) {
            if (locationInfo!=null){
                try {
                    val number=repository.insertLocation(locationInfo)
                    if (number.toInt() >= 1){
                        mutableMsg.emit("done")
                    }
                    else{
                        mutableMsg.emit( "no rec")
                    }
                }
                catch (ex:Exception){
                    mutableMsg.emit(ex.localizedMessage?:"no rec")
                }
            }
        }
    }
}

class LocationsViewModelFactory(private val repository: WeatherRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LocationsViewModel(repository) as T
    }
}