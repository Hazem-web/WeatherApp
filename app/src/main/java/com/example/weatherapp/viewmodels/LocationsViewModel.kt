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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class LocationsViewModel(private val repository: WeatherRepository): ViewModel() {
    private val mutableLocations: MutableStateFlow<Results<List<LocationInfo>>> = MutableStateFlow(
        Results.Loading)
    val locations: StateFlow<Results<List<LocationInfo>>> = mutableLocations

    private val mutableMsg: MutableStateFlow<String> = MutableStateFlow("Loading")
    val massage:StateFlow<String> = mutableMsg

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

    fun deleteLocation(locationInfo: LocationInfo, context: Context){
        viewModelScope.launch(Dispatchers.IO) {
                try {
                    val number = repository.deleteLocation(locationInfo)
                    if (number == 0) {
                        mutableMsg.value = context.getString(R.string.not_found)
                    }else {
                        mutableMsg.value= context.getString(R.string.deleted)
                    }
                }
                catch (ex:Exception){
                    mutableMsg.value = ex.localizedMessage?:context.getString(R.string.not_rec)
                }
            }


    }

    fun returnLocation(locationInfo: LocationInfo?, context: Context){
        viewModelScope.launch(Dispatchers.IO) {
            if (locationInfo!=null){
                try {
                    val number=repository.insertLocation(locationInfo)
                    if (number.toInt() == 1){
                        mutableMsg.value= context.getString(R.string.returned)
                    }
                    else{
                        mutableMsg.value= context.getString(R.string.not_rec)
                    }
                }
                catch (ex:Exception){
                    mutableMsg.value= ex.localizedMessage?:context.getString(R.string.not_rec)
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