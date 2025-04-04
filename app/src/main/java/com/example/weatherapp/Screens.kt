package com.example.weatherapp

import android.annotation.SuppressLint
import android.location.Geocoder
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonColors
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.example.weatherapp.data.models.ForecastWeatherResponse
import com.example.weatherapp.data.models.GeocodingResponse
import com.example.weatherapp.data.models.LocationInfo
import com.example.weatherapp.data.models.Notification
import com.example.weatherapp.data.models.NotificationType
import com.example.weatherapp.data.models.Results
import com.example.weatherapp.data.models.Weather
import com.example.weatherapp.data.models.WeatherDto
import com.example.weatherapp.data.models.WeatherResponse
import com.example.weatherapp.ui.theme.DarkBlue
import com.example.weatherapp.ui.theme.TransparentWhite
import com.example.weatherapp.viewmodels.DetailsViewModel
import com.example.weatherapp.viewmodels.HomeMapViewModel
import com.example.weatherapp.viewmodels.HomeViewModel
import com.example.weatherapp.viewmodels.LocationsViewModel
import com.example.weatherapp.viewmodels.MapsViewModel
import com.example.weatherapp.viewmodels.NotificationsViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.datetime.date.datepicker
import com.vanpra.composematerialdialogs.datetime.time.timepicker
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Serializable
sealed class ScreenRoute{
    @Serializable
    object HomeScreen:ScreenRoute()

    @Serializable
    object PlacesScreen:ScreenRoute()

    @Serializable
    object NotificationScreen:ScreenRoute()

    @Serializable
    object SettingsScreen:ScreenRoute()

    @Serializable
    data class DetailsScreen(val lat:Double,val lon:Double):ScreenRoute()

    @Serializable
    object MapScreen:ScreenRoute()

    @Serializable
    object HomeMapScreen:ScreenRoute()
}


@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun HomePage(
    viewModel: HomeViewModel,
    degrees: Degrees,
    speed: Speed,
    isNight: Boolean,
    info: WeatherDto
){
    viewModel.getAll(info.lat,info.lon,Locale.getDefault().language.lowercase())
    val weatherState = viewModel.weather.collectAsState()
    val forecastState= viewModel.forecast.collectAsState()
    val scope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }
    val unknownError=stringResource(R.string.not_rec)
    val error= stringResource(R.string.error)
    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState){
            Snackbar(it, containerColor = MaterialTheme.colorScheme.primary)
        } },
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Color.Transparent),
        containerColor = Color.Transparent
    ) { innerPadding->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(all = 10.dp)
                .fillMaxWidth()
                .background(color = Color.Transparent),
            verticalArrangement = Arrangement.spacedBy(15.dp),
            horizontalAlignment = Alignment.CenterHorizontally
            ) {
            when(weatherState.value){
                is Results.Loading->{
                    CircularProgressIndicator(
                        modifier = Modifier
                            .width(64.dp)
                            .height(64.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
                is Results.Success->{
                    val data=(weatherState.value as Results.Success<WeatherResponse>).data
                    WeatherDetails(data,degrees,isNight)
                    WeatherInfo(data,speed)
                }
                else-> {
                    scope.launch {
                        snackBarHostState.showSnackbar(
                            message = "$error: ${(weatherState.value as Results.Failure).error.localizedMessage?:unknownError} ",
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            }
            when(forecastState.value){
                is Results.Loading->{
                    CircularProgressIndicator(
                        modifier = Modifier
                            .width(64.dp)
                            .height(64.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
                is Results.Success->{
                    val data=(forecastState.value as Results.Success<ForecastWeatherResponse>).data
                    ForecastDetails(data,degrees,isNight)
                }
                else-> {
                    scope.launch {
                        snackBarHostState.showSnackbar(
                            message = "$error: ${(forecastState.value as Results.Failure).error.localizedMessage?:unknownError} ",
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            }
        }
    }
}

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun DetailsPage(
    viewModel: DetailsViewModel,
    degrees: Degrees,
    speed: Speed,
    isNight: Boolean,
    info: WeatherDto,
    back:()->Unit
){
    viewModel.getAll(info.lat,info.lon,Locale.getDefault().language.lowercase())
    val weatherState = viewModel.weather.collectAsState()
    val forecastState= viewModel.forecast.collectAsState()
    val scope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }
    val unknownError=stringResource(R.string.not_rec)
    val error= stringResource(R.string.error)
    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState){
            Snackbar(it, containerColor = MaterialTheme.colorScheme.primary)
        } },
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Color.Transparent),
        containerColor = Color.Transparent
    ) { innerPadding->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(all = 10.dp)
                .fillMaxWidth()
                .background(color = Color.Transparent),
            verticalArrangement = Arrangement.spacedBy(15.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when(weatherState.value){
                is Results.Loading->{
                    CircularProgressIndicator(
                        modifier = Modifier
                            .width(64.dp)
                            .height(64.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
                is Results.Success->{
                    val data=(weatherState.value as Results.Success<WeatherResponse>).data
                    Row(
                        horizontalArrangement = Arrangement.Absolute.Left,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            painterResource(R.drawable.arrow_back),
                            contentDescription = stringResource(R.string.back),
                            modifier = Modifier
                                .size(30.dp)
                                .clickable {
                                    back()
                                },
                            tint = Color.White
                            )
                    }
                    WeatherDetails(data,degrees,isNight)
                    WeatherInfo(data,speed)
                }
                else-> {
                    scope.launch {
                        snackBarHostState.showSnackbar(
                            message = "$error: ${(weatherState.value as Results.Failure).error.localizedMessage?:unknownError} ",
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            }
            when(forecastState.value){
                is Results.Loading->{
                    CircularProgressIndicator(
                        modifier = Modifier
                            .width(64.dp)
                            .height(64.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
                is Results.Success->{
                    val data=(forecastState.value as Results.Success<ForecastWeatherResponse>).data
                    ForecastDetails(data,degrees,isNight)
                }
                else-> {
                    scope.launch {
                        snackBarHostState.showSnackbar(
                            message = "$error: ${(forecastState.value as Results.Failure).error.localizedMessage ?: unknownError} ",
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            }
        }
    }
}

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun LocationsPage(locationsViewModel: LocationsViewModel, toDetails:(WeatherDto)->Unit, toMaps:()->Unit){
    locationsViewModel.getLocations()
    val locationState=locationsViewModel.locations.collectAsState()
    val msgState=locationsViewModel.massage.collectAsState()
    val scope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }
    val unknownError=stringResource(R.string.not_rec)
    val currentRemoved= remember { mutableStateOf<LocationInfo?>(null) }
    val error= stringResource(R.string.error)
    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState, ){
            Snackbar(it, containerColor = MaterialTheme.colorScheme.primary)
        } },
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Color.Transparent),
        containerColor = Color.Transparent,
        floatingActionButton = {
            LocationFloatingActionButton{
                toMaps()
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ){ innerPadding->
        when(msgState.value){
            Constants.DELETED.value->{
                val undo= stringResource(R.string.undo)
                val string=stringResource(R.string.deleted)
                scope.launch {
                  val action= snackBarHostState.showSnackbar(string,undo,)
                    when(action){
                        SnackbarResult.ActionPerformed ->{
                            locationsViewModel.returnLocation(currentRemoved.value)
                        }
                        else->{

                        }
                    }
                }
            }
            Constants.DONE.value->{
                val string=stringResource(R.string.returned)
                scope.launch {
                    snackBarHostState.showSnackbar(string,)
                }
            }
            Constants.NOT_REC.value->{
                scope.launch {
                    snackBarHostState.showSnackbar("$error: $unknownError",)
                }
            }
            Constants.NO_ITEM.value->{
                val string=stringResource(R.string.not_found)
                scope.launch {
                    snackBarHostState.showSnackbar("$error: $string",)
                }
            }
            Constants.LOADING.value->{

            }
            else->{
                scope.launch {
                    snackBarHostState.showSnackbar(msgState.value,)
                }
            }
        }
        when(locationState.value) {
            is Results.Loading -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .width(64.dp)
                            .height(64.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }

            is Results.Success -> {
                val items=(locationState.value as Results.Success).data?: listOf<LocationInfo>()
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(items){
                        PlacesItem(
                            location = it,
                            onClick = {
                                toDetails(WeatherDto(it.lat,it.long))
                            },
                            onRemove = {
                                scope.launch {
                                    currentRemoved.value=it
                                    locationsViewModel.deleteLocation(it)
                                }
                            }
                            )
                    }
                }
            }
            is Results.Failure->{
                scope.launch {
                    snackBarHostState.showSnackbar(
                        message = "$error: ${(locationState.value as Results.Failure).error.localizedMessage ?: unknownError} ",
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

}


@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun NotificationsPage(notificationsViewModel: NotificationsViewModel){
    notificationsViewModel.getNotifications()
    val context= LocalContext.current
    val locationState=notificationsViewModel.notifications.collectAsState()
    val msgState=notificationsViewModel.massage.collectAsState()
    val scope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }
    val unknownError=stringResource(R.string.not_rec)
    val currentRemoved= remember { mutableStateOf<Notification?>(null) }
    val error= stringResource(R.string.error)
    var showDialog by remember { mutableStateOf(false) }
    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState, snackbar = {
            Snackbar(it, containerColor = MaterialTheme.colorScheme.primary)
        }) },
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Transparent),
        containerColor = Color.Transparent,
        floatingActionButton = {
            NotificationFloatingActionButton {
                showDialog=true
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ){ innerPadding->
        when(msgState.value){
            Constants.DELETED.value->{
                val undo= stringResource(R.string.undo)
                val string=stringResource(R.string.deleted)
                scope.launch {
                    val action= snackBarHostState.showSnackbar(string,undo,)
                    when(action){
                        SnackbarResult.ActionPerformed ->{
                            notificationsViewModel.addNotification(currentRemoved.value,context)
                        }
                        else->{

                        }
                    }
                }
            }
            Constants.DONE.value->{
                val string=stringResource(R.string.returned)
                scope.launch {
                    snackBarHostState.showSnackbar(string,)
                }
            }
            Constants.NOT_REC.value->{
                scope.launch {
                    snackBarHostState.showSnackbar("$error: $unknownError",)
                }
            }
            Constants.NO_ITEM.value->{
                val string=stringResource(R.string.not_found)
                scope.launch {
                    snackBarHostState.showSnackbar("$error: $string",)
                }
            }
            Constants.LOADING.value->{

            }
            else->{
                scope.launch {
                    snackBarHostState.showSnackbar(msgState.value,)
                }
            }
        }
        when(locationState.value) {
            is Results.Loading -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .width(64.dp)
                            .height(64.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }

            is Results.Success -> {
                val items=(locationState.value as Results.Success).data?: listOf<Notification>()
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(items){
                        NotificationItem(
                            notification = it,
                            onRemove = {
                                scope.launch {
                                    currentRemoved.value=it
                                    notificationsViewModel.deleteNotification(it, context)
                                }
                            }
                        )
                    }
                }
            }
            is Results.Failure->{
                scope.launch {
                    snackBarHostState.showSnackbar(
                        message = "$error: ${(locationState.value as Results.Failure).error.localizedMessage ?: unknownError} ",
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
        if (showDialog) {
            CustomDateTimePickerDialog(
                onDismiss = { showDialog = false },
                onDateTimeSelected = { date, time, type -> notificationsViewModel.addNotification(Notification(time=time, date = date, type = type),context) }
            )
        }
    }

}

@Composable
fun MapsPage(mapsViewModel: MapsViewModel, returnBack:()->Unit){
    val scope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }
    val context= LocalContext.current
    val place = LatLng(0.0, 0.0)
    val markerState = rememberMarkerState(position = place)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(place, 10f)
    }
    val searchText= remember { mutableStateOf("") }
    val placesState=mapsViewModel.places.collectAsState()
    val locations= remember { mutableStateOf(listOf<GeocodingResponse>()) }
    val current= remember { mutableStateOf<LocationInfo?>(null) }
    val geocoderAr= Geocoder(context,Locale("ar"))
    val geocoderEn= Geocoder(context,Locale("en"))
    val text= stringResource(R.string.error)+": " + stringResource(R.string.select_valid)
    when (placesState.value ){
        is Results.Success->{
            locations.value= (placesState.value as Results.Success<List<GeocodingResponse>>).data?: listOf<GeocodingResponse>()
        }
        else->{

        }

    }
    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState, snackbar = {
            Snackbar(it, containerColor = MaterialTheme.colorScheme.primary)
        }) },
        modifier = Modifier
            .fillMaxSize(),
        floatingActionButton = {
            MapsFloatingActionButton(
                isClickable = current.value!=null
            ) {
                if (current.value!=null){
                    mapsViewModel.addLocation(current.value)
                    returnBack()
                }
                else{

                    scope.launch {
                        snackBarHostState.showSnackbar(text, duration = SnackbarDuration.Short)
                    }
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Start,){innerPadding->
        GoogleMap(
            modifier = Modifier.padding(innerPadding).fillMaxSize(),
            cameraPositionState = cameraPositionState,
            onMapClick = {
                markerState.position=it
                scope.launch(Dispatchers.IO) {
                    val list=geocoderAr.getFromLocation(it.latitude,it.longitude,1)?: listOf()
                    val list2=geocoderEn.getFromLocation(it.latitude,it.longitude,1)?: listOf()
                    if (list.isNotEmpty() &&list2.isNotEmpty()){
                        current.value= LocationInfo(it.longitude,it.latitude, list2[0].getAddressLine(0),list[0].getAddressLine(0),list2[0].countryName,list2[0].countryCode)
                    }
                }
            }
        ) {
            Marker(
                state = markerState,
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(innerPadding).fillMaxWidth(0.9f)
        ) {
            TextField(
                value = searchText.value,
                onValueChange = {
                    searchText.value=it
                    if(it.isNotBlank())
                        mapsViewModel.getPlaces(it)
                    else
                        locations.value= listOf()
                },
                placeholder = {
                    Text(stringResource(R.string.search))
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(0.8f)
                )
            LazyColumn(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                items(locations.value){
                    Text(text=if (Locale.getDefault().language=="en"){(it.localNames?.english?:it.name)+", "+it.country} else{(it.localNames?.arabic?:it.name)+", "+it.country},
                        modifier = Modifier
                            .background(Color.Black)
                            .padding(vertical = 5.dp)
                            .fillMaxWidth(0.8f)
                            .clickable {
                                current.value=LocationInfo(it.longitude,it.latitude,it.localNames?.english?:it.name,it.localNames?.arabic?:it.name,it.country,it.country)
                                markerState.position= LatLng(it.latitude,it.longitude)
                                locations.value= listOf()
                                searchText.value=""
                            },
                        color = Color.Black)
                }
            }

        }

    }
}

@Composable
fun HomeMapPage(homeMapViewModel: HomeMapViewModel, saveLocation:(LocationInfo)->Unit){
    val scope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }
    val context= LocalContext.current
    val place = LatLng(0.0, 0.0)
    val markerState = rememberMarkerState(position = place)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(place, 10f)
    }
    val searchText= remember { mutableStateOf("") }
    val placesState=homeMapViewModel.places.collectAsState()
    val locations= remember { mutableStateOf(listOf<GeocodingResponse>()) }
    val current= remember { mutableStateOf<LocationInfo?>(null) }
    val geocoderAr= Geocoder(context,Locale("ar"))
    val geocoderEn= Geocoder(context,Locale("en"))
    val text= stringResource(R.string.error)+": " + stringResource(R.string.select_valid)
    when (placesState.value ){
        is Results.Success->{
            locations.value= (placesState.value as Results.Success<List<GeocodingResponse>>).data?: listOf<GeocodingResponse>()
        }
        else->{

        }

    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState){
            Snackbar(it, containerColor = MaterialTheme.colorScheme.primary)
        } },
        modifier = Modifier
            .fillMaxSize(),
        floatingActionButton = {
            MapsFloatingActionButton(
                isClickable = current.value!=null
            ) {
                if (current.value!=null){
                    saveLocation(current.value!!)
                }
                else{

                    scope.launch {
                        snackBarHostState.showSnackbar(text, duration = SnackbarDuration.Short)
                    }
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Start){innerPadding->
        GoogleMap(
            modifier = Modifier.padding(innerPadding).fillMaxSize(),
            cameraPositionState = cameraPositionState,
            onMapClick = {
                markerState.position=it
                val list=geocoderAr.getFromLocation(it.latitude,it.longitude,1)?: listOf()
                val list2=geocoderEn.getFromLocation(it.latitude,it.longitude,1)?: listOf()
                if (list.isNotEmpty() &&list2.isNotEmpty()){
                    current.value= LocationInfo(it.longitude,it.latitude, list2[0].getAddressLine(0),list[0].getAddressLine(0),list2[0].countryName,list2[0].countryCode)
                }
            }
        ) {
            Marker(
                state = markerState,
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(innerPadding).fillMaxWidth(0.9f)
        ) {
            TextField(
                value = searchText.value,
                onValueChange = {
                    searchText.value=it
                    if(it.isNotBlank())
                        homeMapViewModel.getPlaces(it)
                    else{
                        locations.value= listOf()
                    }
                },
                placeholder = {
                    Text(stringResource(R.string.search))
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(0.8f)
            )
            LazyColumn(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                items(locations.value){
                    Text(text=if (Locale.getDefault().language=="en"){(it.localNames?.english?:it.name)+", "+it.country} else{(it.localNames?.arabic?:it.name)+", "+it.country},
                        modifier = Modifier
                            .background(Color.Black)
                            .padding(vertical = 5.dp)
                            .fillMaxWidth(0.8f)
                            .clickable {
                                current.value=LocationInfo(it.longitude,it.latitude,it.localNames?.english?:it.name,it.localNames?.arabic?:it.name,it.country,it.country)
                                markerState.position= LatLng(it.latitude,it.longitude)
                                locations.value= listOf()
                                searchText.value=""
                                cameraPositionState.position=CameraPosition.fromLatLngZoom(markerState.position, 10f)
                            },
                        color = Color.Black)
                }
            }

        }

    }
}

@Composable
fun SettingsPage(
    isNight: Boolean,
    language: String,
    speed: Speed,
    degrees: Degrees,
    mode:String,
    changeColor: (Boolean)->Unit,
    changeLanguage: (String) -> Unit,
    changeSpeed: (Speed)->Unit,
    changeDegrees: (Degrees) -> Unit,
    changeMode: (String)->Unit,
    innerPaddingValues: PaddingValues
    ){
    val selectedColor= remember { mutableStateOf(isNight) }
    val selectedLanguage= remember { mutableStateOf(language) }
    val selectedSpeed= remember { mutableStateOf(speed) }
    val selectedDegree= remember { mutableStateOf(degrees) }
    val selectedMode= remember { mutableStateOf(mode) }
    val scrollState= rememberScrollState()
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues = innerPaddingValues)
            .padding(horizontal =10.dp,)
            .verticalScroll(scrollState),
    ) {
        Spacer(Modifier.height(10.dp))
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(brush = Brush.linearGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.primary,
                    Color.White.copy(alpha = 0.4f)
                )
            ))
                .fillMaxWidth()
                .padding(5.dp)

        ) {
            Text(stringResource(R.string.mode), fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (selectedMode.value == Constants.LOCATION.value),
                    onClick = {
                        if (selectedMode.value != Constants.LOCATION.value) {
                            changeMode(Constants.LOCATION.value)
                            selectedMode.value=Constants.LOCATION.value
                        }
                    },
                    colors = RadioButtonColors(
                        selectedColor = Color.Magenta,
                        unselectedColor = Color.White,
                        disabledSelectedColor = Color.Black,
                        disabledUnselectedColor = Color.Black
                    )
                )
                Spacer(Modifier.width(5.dp))
                Text(text = stringResource(R.string.gps))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (selectedMode.value == Constants.MAP.value),
                    onClick = {
                            changeMode(Constants.MAP.value)
                            selectedMode.value=Constants.MAP.value

                    },
                    colors = RadioButtonColors(
                        selectedColor = Color.Magenta,
                        unselectedColor = Color.White,
                        disabledSelectedColor = Color.Black,
                        disabledUnselectedColor = Color.Black
                    )
                )
                Spacer(Modifier.width(5.dp))
                Text(text = stringResource(R.string.map))

            }
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(brush = Brush.linearGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.primary,
                    Color.White.copy(alpha = 0.4f)
                )
            ))
                .padding(5.dp)
                .fillMaxWidth()
        ) {
            Text(stringResource(R.string.degree), fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (selectedDegree.value == Degrees.CELSIUS),
                    onClick = {
                        if (selectedDegree.value != Degrees.CELSIUS) {
                            changeDegrees(Degrees.CELSIUS)
                            selectedDegree.value=Degrees.CELSIUS
                        }
                    },
                    colors = RadioButtonColors(
                        selectedColor = Color.Magenta,
                        unselectedColor = Color.White,
                        disabledSelectedColor = Color.Black,
                        disabledUnselectedColor = Color.Black
                    )
                )
                Spacer(Modifier.width(5.dp))
                Text(text = stringResource(R.string.full_celsius))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (selectedDegree.value == Degrees.KELVIN),
                    onClick = {
                        if (selectedDegree.value != Degrees.KELVIN){
                            changeDegrees(Degrees.KELVIN)
                            selectedDegree.value=Degrees.KELVIN
                        }
                    },
                    colors = RadioButtonColors(
                        selectedColor = Color.Magenta,
                        unselectedColor = Color.White,
                        disabledSelectedColor = Color.Black,
                        disabledUnselectedColor = Color.Black
                    )
                )
                Spacer(Modifier.width(5.dp))
                Text(text = stringResource(R.string.full_kelvin))

            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (selectedDegree.value == Degrees.FAHRENHEIT),
                    onClick = {
                        if (selectedDegree.value != Degrees.FAHRENHEIT){
                            changeDegrees(Degrees.FAHRENHEIT)
                            selectedDegree.value=Degrees.FAHRENHEIT
                        }
                    },
                    colors = RadioButtonColors(
                        selectedColor = Color.Magenta,
                        unselectedColor = Color.White,
                        disabledSelectedColor = Color.Black,
                        disabledUnselectedColor = Color.Black
                    )
                )
                Spacer(Modifier.width(5.dp))
                Text(text = stringResource(R.string.full_fahrenheit))
            }
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(brush = Brush.linearGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.primary,
                    Color.White.copy(alpha = 0.4f)
                )
            ))
                .padding(5.dp)
                .fillMaxWidth()
        ) {
            Text(stringResource(R.string.speed), fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (selectedSpeed.value == Speed.MILE),
                    onClick = {
                        if (selectedSpeed.value != Speed.MILE) {
                            changeSpeed(Speed.MILE)
                            selectedSpeed.value=Speed.MILE
                        }
                    },
                    colors = RadioButtonColors(
                        selectedColor = Color.Magenta,
                        unselectedColor = Color.White,
                        disabledSelectedColor = Color.Black,
                        disabledUnselectedColor = Color.Black
                    )
                )
                Spacer(Modifier.width(5.dp))
                Text(text = stringResource(R.string.mile_hour))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (selectedSpeed.value == Speed.METER),
                    onClick = {
                        if (selectedSpeed.value != Speed.METER){
                            changeSpeed(Speed.METER)
                            selectedSpeed.value=Speed.METER
                        }
                    },
                    colors = RadioButtonColors(
                        selectedColor = Color.Magenta,
                        unselectedColor = Color.White,
                        disabledSelectedColor = Color.Black,
                        disabledUnselectedColor = Color.Black
                    )
                )
                Spacer(Modifier.width(5.dp))
                Text(text = stringResource(R.string.meter_sec))
            }
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(brush = Brush.linearGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.primary,
                    Color.White.copy(alpha = 0.4f)
                )
            ))
                .padding(5.dp)
                .fillMaxWidth()
        ) {
            Text(stringResource(R.string.theme), fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (!selectedColor.value),
                    onClick = {
                        if (selectedColor.value) {
                            changeColor(false)
                            selectedColor.value=false
                        }
                    },
                    colors = RadioButtonColors(
                        selectedColor = Color.Magenta,
                        unselectedColor = Color.White,
                        disabledSelectedColor = Color.Black,
                        disabledUnselectedColor = Color.Black
                    )
                )
                Spacer(Modifier.width(5.dp))
                Text(text = stringResource(R.string.light))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (selectedColor.value),
                    onClick = {
                        if (!selectedColor.value){
                            changeColor(true)
                            selectedColor.value=true
                        }
                    },
                    colors = RadioButtonColors(
                        selectedColor = Color.Magenta,
                        unselectedColor = Color.White,
                        disabledSelectedColor = Color.Black,
                        disabledUnselectedColor = Color.Black
                    )
                )
                Spacer(Modifier.width(5.dp))
                Text(text = stringResource(R.string.dark))
            }
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(brush = Brush.linearGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.primary,
                    Color.White.copy(alpha = 0.4f)
                )
            ))
                .padding(5.dp)
                .fillMaxWidth()
        ) {
            Text(stringResource(R.string.lang), fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (selectedLanguage.value==Constants.ARABIC.value),
                    onClick = {
                        if (selectedLanguage.value!=Constants.ARABIC.value) {
                            changeLanguage(Constants.ARABIC.value)
                            selectedLanguage.value=Constants.ARABIC.value
                        }
                    },
                    colors = RadioButtonColors(
                        selectedColor = Color.Magenta,
                        unselectedColor = Color.White,
                        disabledSelectedColor = Color.Black,
                        disabledUnselectedColor = Color.Black
                    )
                )
                Spacer(Modifier.width(5.dp))
                Text(text = stringResource(R.string.arabic))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (selectedLanguage.value==Constants.ENGLISH.value),
                    onClick = {
                        if (selectedLanguage.value!=Constants.ENGLISH.value){
                            changeLanguage(Constants.ENGLISH.value)
                            selectedLanguage.value=Constants.ENGLISH.value
                        }
                    },
                    colors = RadioButtonColors(
                        selectedColor = Color.Magenta,
                        unselectedColor = Color.White,
                        disabledSelectedColor = Color.Black,
                        disabledUnselectedColor = Color.Black
                    )
                )
                Spacer(Modifier.width(5.dp))
                Text(text = stringResource(R.string.english))
            }
        }
        Spacer(Modifier.height(10.dp))

    }
}


@Composable
fun LocationFloatingActionButton(onClick:  () -> Unit) {

    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = Color.White
    ) {
        Icon(painter = painterResource(R.drawable.location_add), contentDescription = stringResource(R.string.notify_add))
    }
}

@Composable
fun NotificationFloatingActionButton(onClick:  () -> Unit) {

    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = Color.White
    ) {
        Icon(painter = painterResource(R.drawable.add_notification), contentDescription = stringResource(R.string.notify_add))
    }
}

@Composable
fun MapsFloatingActionButton( isClickable:Boolean, onClick:  () -> Unit) {

    FloatingActionButton(
        onClick = {
            if (isClickable){
                onClick()
            }
        },
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = Color.White
    ) {
        Icon(painter = painterResource(R.drawable.done), contentDescription = stringResource(R.string.locatin_add))
    }
}

@Composable
fun CustomDateTimePickerDialog(
    onDismiss: () -> Unit,
    onDateTimeSelected: (Long,Long,NotificationType) -> Unit,
) {
    val calendar = remember { Calendar.getInstance() }

    var selectedDate by remember { mutableStateOf<Long>(0) }
    var selectedTime by remember { mutableStateOf<Long>(0) }
    var selectedType by remember { mutableStateOf<NotificationType>(NotificationType.NOTIFICATION) }

    val dateFormatter=SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val timeFormatter=SimpleDateFormat("HH:mm", Locale.getDefault())

    val dateDialogState= rememberMaterialDialogState()
    val timeDialogState= rememberMaterialDialogState()


    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .background(Color.Black)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .background(Color.Black)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(stringResource(R.string.date_time), fontSize = 18.sp, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = dateFormatter.format(Date(selectedDate)),
                        modifier = Modifier.clickable {
                            dateDialogState.show()
                        }
                    )

                    Text(
                        text = timeFormatter.format(Date(selectedTime)),
                        modifier = Modifier.clickable {
                            timeDialogState.show()
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(stringResource(R.string.pick_noti_type), fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    RadioButton(
                        selected = (selectedType==NotificationType.NOTIFICATION),
                        onClick = {
                            selectedType=NotificationType.NOTIFICATION
                        },
                        colors = RadioButtonColors(
                            selectedColor = Color.White,
                            unselectedColor = DarkBlue,
                            disabledSelectedColor = Color.Black,
                            disabledUnselectedColor = Color.Black
                        )
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(text = stringResource(R.string.notify))
                    Spacer(Modifier.width(10.dp))
                    RadioButton(
                        selected = (selectedType==NotificationType.ALARM),
                        onClick = {
                            selectedType=NotificationType.ALARM
                        },
                        colors = RadioButtonColors(
                            selectedColor = Color.White,
                            unselectedColor = DarkBlue,
                            disabledSelectedColor = Color.Black,
                            disabledUnselectedColor = Color.Black
                        )
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(text = stringResource(R.string.alarm))
                }
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }
                    Button(onClick = {
                        onDateTimeSelected(selectedDate,selectedTime,selectedType)
                        onDismiss()
                    },
                     enabled = calendar.timeInMillis<selectedDate+selectedTime   ) {
                        Text(stringResource(R.string.submit))
                    }
                }
            }
        }
        MaterialDialog(
            dialogState = dateDialogState,
            buttons = {
                positiveButton(stringResource(R.string.submit))
                negativeButton(stringResource(R.string.cancel))
            },
        ) {
          datepicker(
              initialDate = LocalDate.now(),
              title = stringResource(R.string.pick_date),
              allowedDateValidator = {
                  it>=LocalDate.now()
              }
          ){
              selectedDate=it.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
          }
        }
        MaterialDialog(
            dialogState = timeDialogState,
            buttons = {
                positiveButton(stringResource(R.string.submit))
                negativeButton(stringResource(R.string.cancel))
            },
        ) {
            timepicker(
                is24HourClock = true,
                title = stringResource(R.string.pick_time),

            ){
                selectedTime= (it.toNanoOfDay()/1000000)-(2*60*60*1000)
            }
        }
    }
}


@Composable
fun TemperatureString(
    temp:Double,
    degrees: Degrees,
    modifier: Modifier=Modifier,
    text:String="",
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    textAlign: TextAlign? = null,
    style: TextStyle = LocalTextStyle.current
){
    val value=when(degrees){
        Degrees.KELVIN -> stringResource(R.string.kelvin)
        Degrees.CELSIUS -> stringResource(R.string.celsius)
        Degrees.FAHRENHEIT -> stringResource(R.string.fahrenheit)
    }
    Text(
        text = "$text${getTemp(temp, degrees)}° $value",
        modifier = modifier,
        color = color,
        fontSize= fontSize,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        fontFamily= fontFamily,
        textAlign = textAlign,
        style = style
    )
}

fun getIcon(weather: Weather?, isNight:Boolean): Int {
    if (weather==null){
        return R.drawable.sun
    }
    return when(weather.main){
        WeatherModes.THUNDER.value -> R.drawable.thander
        WeatherModes.CLEAR.value -> if(isNight) R.drawable.moon else R.drawable.sun
        WeatherModes.SNOW.value -> R.drawable.snow
        WeatherModes.CLOUDS.value -> if(weather.id==801) {
            val output:Int= if (isNight)
                R.drawable.bit_cloudy_dark
            else
                R.drawable.bit_cloudy
            output
        } else R.drawable.cloudy
        WeatherModes.RAIN.value -> if (weather.icon=="13d") R.drawable.snow else R.drawable.rainy
        else -> R.drawable.wind
    }
}

fun getTemp(temp:Double,degrees: Degrees):Int{
    return when(degrees){
        Degrees.KELVIN -> temp.toInt()
        Degrees.CELSIUS -> (temp-273.15).toInt()
        Degrees.FAHRENHEIT -> ((temp-273.15) * 1.8 + 32).toInt()
    }
}

fun getSpeed(value:Double, speed: Speed):Double{
    return when(speed){
        Speed.METER -> value
        Speed.MILE -> value * 2.236936
    }
}

@Composable
fun WeatherDetails(weatherResponse: WeatherResponse?, degrees: Degrees, isNight: Boolean){
    val calendar = Calendar.getInstance()
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    Column(
        modifier = Modifier
            .background(color = Color.Transparent)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CountryName(weatherResponse?.name?:"",weatherResponse?.place?.country?:"")
        calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault())
            ?.let {
                Text(
                    text = "$it - ${formatter.format(calendar.time)}"
                )
            }
        Image(
            painter = painterResource(id = getIcon(
                weatherResponse?.weather?.get(0),
                isNight
            )),
            contentDescription = (weatherResponse?.weather?.get(0)?.description ?:"") + stringResource(R.string.icon),
            modifier = Modifier
                .width(138.dp)
                .height(91.dp),
        )
        TemperatureString(
            temp = weatherResponse?.details?.temp?:0.00,
            degrees = degrees,
            fontWeight = FontWeight.Bold,
            fontSize = 48.sp
        )
        TemperatureDetails(
            min = weatherResponse?.details?.min?:0.0,
            max = weatherResponse?.details?.max?:0.0,
            feels = weatherResponse?.details?.feels?:0.0,
            degrees = degrees
        )
        Text(
            text = weatherResponse?.weather?.get(0)?.description?:"",
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun WeatherInfo(weatherResponse: WeatherResponse?, speed: Speed){
        Row(
            modifier = Modifier
                .background(color = Color.Transparent)
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(5.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            Color.White.copy(alpha = 0.4f)
                        )
                    )
                )
                .padding(horizontal = 5.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ItemInfo(
                stringResource(R.string.humidity),
                "${weatherResponse?.details?.humidity ?: ""}%",
                R.drawable.humidity_icon
            )
            ItemInfo(
                stringResource(R.string.clouds),
                "${weatherResponse?.clouds?.all ?:""}%",
                R.drawable.clouds_icon
            )
            ItemInfo(
                stringResource(R.string.pressure),
                "${weatherResponse?.details?.pressure ?:""}\n${stringResource(R.string.hpa)}",
                R.drawable.pressure_icon
            )
            WindInfo(weatherResponse?.wind?.speed ?: 0.0, speed)
        }

}

@Composable
fun TemperatureDetails(
    min:Double,
    max:Double,
    feels:Double,
    degrees: Degrees
){
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TemperatureString(
            temp = min,
            degrees = degrees,
            fontSize = 12.sp,
            fontWeight = FontWeight.Light,
            color = TransparentWhite,
            text = stringResource(R.string.weather_min)+":"
        )
        TemperatureString(
            temp = feels,
            degrees = degrees,
            fontSize = 12.sp,
            fontWeight = FontWeight.Light,
            color = TransparentWhite,
            text = stringResource(R.string.feels_like)+":"
        )
        TemperatureString(
            temp = max,
            degrees = degrees,
            fontSize = 12.sp,
            fontWeight = FontWeight.Light,
            color = TransparentWhite,
            text = stringResource(R.string.weather_max)+":"
        )
    }
}

@Composable
private fun CountryName(place:String, country:String) {
    Row(
        modifier = Modifier.background(color = Color.Transparent),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = "$place,",
            fontWeight = FontWeight.Bold,
            fontSize = 30.sp
        )
        Text(
            text = country,
            fontWeight = FontWeight.Normal,
            fontSize = 30.sp
        )
    }
}

@Composable
fun ItemInfo(text: String, value: String, icon:Int){
    Row(

        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(icon),
                contentDescription = text + stringResource(R.string.icon),
                modifier = Modifier
                    .width(24.dp)
                    .height(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text= text,
                textAlign = TextAlign.Center,
                fontSize = 10.sp,
            )
        }
        Text(text= value)
    }
}

@Composable
fun WindInfo(value: Double, speed: Speed){
    val speedValue=when(speed){
        Speed.METER-> stringResource(R.string.meter_sec)
        else -> stringResource(R.string.mile_hour)
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column() {
            Image(
                painter = painterResource(R.drawable.wind_icon),
                contentDescription = stringResource(R.string.wind) + stringResource(R.string.icon),
                modifier = Modifier
                    .width(24.dp)
                    .height(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text= stringResource(R.string.wind),
                textAlign = TextAlign.Center,
                fontSize = 10.sp,
            )
        }
        Text(text= "${String.format("%.2f", getSpeed(value,speed))}\n$speedValue")
    }
}

@Composable
fun ForecastItem(date:String, icon: Int, desc:String, degree:String){
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clip(RoundedCornerShape(7.dp))
                .background(MaterialTheme.colorScheme.secondary)
                .padding(horizontal = 10.dp, vertical = 3.dp)
                ,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = date,
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                fontWeight = FontWeight.Light,
                color = TransparentWhite
            )
            Image(
                painter = painterResource(icon),
                contentDescription = desc,
                modifier = Modifier
                    .width(16.dp)
                    .height(16.dp)
            )
            Text(
                text = degree,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Light
            )
        }

}

@Composable
fun ForecastDetails(forecast: ForecastWeatherResponse?, degrees: Degrees,isNight: Boolean){
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_YEAR, 1)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val tomorrow=  calendar.time.time
    val forecastListState= remember {
        val today=forecast?.forecastList?.filter {
            it.dt*1000<tomorrow
        }
        mutableStateOf(today)
    }
    if ((!forecastListState.value.isNullOrEmpty()) &&(forecast?.forecastList?.contains(forecastListState.value?.get(0))?:true)==false){
        forecastListState.value=forecast?.forecastList?.filter {
            it.dt*1000<tomorrow
        }
    }
    val todayWeightState= remember { mutableStateOf(FontWeight.Bold) }
    val daysWeightState= remember { mutableStateOf(FontWeight.Normal) }
    Column(
        modifier = Modifier
            .background(color = Color.Transparent)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(start = 5.dp, top = 5.dp, bottom = 5.dp)
                .drawBehind {
                    drawLine(
                        color = TransparentWhite,
                        start = androidx.compose.ui.geometry.Offset(0f, size.height),
                        end = androidx.compose.ui.geometry.Offset(size.width, size.height),
                        strokeWidth = 2.dp.toPx()
                    )
                },
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(R.string.today),
                fontWeight = todayWeightState.value,
                modifier = Modifier.clickable {
                    forecastListState.value=forecast?.forecastList?.filter {
                        it.dt*1000<tomorrow
                    }
                    todayWeightState.value=FontWeight.Bold
                    daysWeightState.value= FontWeight.Normal
                }
            )
            Text(
                text = stringResource(R.string.forecast),
                fontWeight = daysWeightState.value,
                modifier = Modifier.clickable {

                    forecastListState.value= forecast?.forecastList?.filter {
                        it.dt*1000>=tomorrow
                    }?.filterIndexed { index, _ ->
                         index%8==0
                    }
                    forecastListState.value
                    todayWeightState.value=FontWeight.Normal
                    daysWeightState.value= FontWeight.Bold
                }
            )
        }
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 5.dp)
        ) {
            items(forecastListState.value?.size?: 0){
                val item= forecastListState.value?.get(it)
                val date=
                if (daysWeightState.value==FontWeight.Bold){
                    getDayOfWeekOld((item?.dt?:0)*1000)
                }
                else{
                     getTimeOld((item?.dt?:0)*1000)
                }
                ForecastItem(
                    date = date,
                    icon = getIcon(
                        weather = item?.weather?.get(0),
                        isNight
                    ),
                    desc = item?.weather?.get(0)?.description ?:"",
                    degree = "${getTemp( temp = item?.details?.temp ?:0.0, degrees = degrees)}°"
                )
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun PlacesItem(location: LocationInfo, onClick: () -> Unit, onRemove: () -> Unit){
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { state ->
            if (state == SwipeToDismissBoxValue.StartToEnd) {
                onRemove()
                true
            } else {
                false
            }
        }
    )
    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {}){
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        Color.White.copy(alpha = 0.4f)
                    )
                ))
                .padding(horizontal = 7.dp, vertical = 10.dp)
                .fillMaxWidth()
                .clickable {
                    onClick()
                },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlideImage(
                model = "https://flagsapi.com/"+location.countryCode.uppercase()+"/flat/64",
                contentDescription = location.country+ stringResource(R.string.flag),
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .size(100.dp),
                contentScale = ContentScale.Crop,
                loading = placeholder(painter = painterResource(R.drawable.placeholder)),
                failure = placeholder(painter = painterResource(R.drawable.placeholder))
            )
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if(Locale.getDefault().language.lowercase()=="ar") location.cityAr else location.city,
                    fontWeight = FontWeight.Bold,
                    fontSize = 30.sp,
                )
                Text(
                    text = location.country,
                )
            }
            Icon(
                painter = painterResource(R.drawable.arrow_forward),
                contentDescription = stringResource(R.string.details),
                modifier = Modifier.size(30.dp),
                tint = Color.White
            )
        }
    }

}

@Composable
fun NotificationItem(notification: Notification, onRemove: () -> Unit){
    val formatter= SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { state ->
            if (state == SwipeToDismissBoxValue.StartToEnd) {
                onRemove()
                true
            } else {
                false
            }
        }
    )
    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {}) {

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        Color.White.copy(alpha = 0.4f)
                    )
                ))
                .padding(horizontal = 7.dp, vertical = 10.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Image(
                painter = if (notification.type == NotificationType.ALARM) painterResource(R.drawable.alarm) else painterResource(
                    R.drawable.notification
                ),
                contentDescription = if (notification.type == NotificationType.ALARM) stringResource(
                    R.string.alarm
                ) else stringResource(R.string.notify),
                modifier = Modifier.size(100.dp),
                contentScale = ContentScale.Crop,
            )
            Spacer(Modifier.width(12.dp))
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (notification.type == NotificationType.ALARM) stringResource(R.string.alarm) else stringResource(
                        R.string.notify
                    ),
                    fontWeight = FontWeight.Bold,
                    fontSize = 30.sp
                )
                Text(
                    text = stringResource(R.string.time) + ": ${formatter.format(Date(notification.date + notification.time))}",
                )
            }

        }
    }
}

fun getDayOfWeekOld(millis: Long): String {
    val sdf = SimpleDateFormat("EEE", Locale.getDefault())
    return sdf.format(Date(millis))
}

fun getTimeOld(millis: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(millis))
}

