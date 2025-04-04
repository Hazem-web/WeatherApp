package com.example.weatherapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.ComponentCaller
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM
import androidx.media3.ui.PlayerView
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.weatherapp.data.local.LocalDataSourceImp
import com.example.weatherapp.data.models.BottomNavItem
import com.example.weatherapp.data.models.WeatherDto
import com.example.weatherapp.data.remote.RemoteDataSourceImp
import com.example.weatherapp.data.repo.WeatherRepositoryImp
import com.example.weatherapp.ui.theme.WeatherAppTheme
import com.example.weatherapp.viewmodels.DetailsViewModel
import com.example.weatherapp.viewmodels.DetailsViewModelFactory
import com.example.weatherapp.viewmodels.HomeMapViewModel
import com.example.weatherapp.viewmodels.HomeMapViewModelFactory
import com.example.weatherapp.viewmodels.HomeViewModel
import com.example.weatherapp.viewmodels.HomeViewModelFactory
import com.example.weatherapp.viewmodels.LocationsViewModel
import com.example.weatherapp.viewmodels.LocationsViewModelFactory
import com.example.weatherapp.viewmodels.MapsViewModel
import com.example.weatherapp.viewmodels.MapsViewModelFactory
import com.example.weatherapp.viewmodels.NotificationsViewModel
import com.example.weatherapp.viewmodels.NotificationsViewModelFactory
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import org.intellij.lang.annotations.Language
import java.util.Locale

const val REQUEST_LOCATION_CODE=1003

class MainActivity : ComponentActivity() {
    lateinit var fusedProvider: FusedLocationProviderClient
    lateinit var weatherDto: MutableState<WeatherDto>
    lateinit var isNight: MutableState<Boolean>
    lateinit var speed: MutableState<Speed>
    lateinit var degrees: MutableState<Degrees>
    lateinit var language: MutableState<String>
    lateinit var mode: MutableState<String>
    lateinit var selectedNavigationIndex:MutableIntState
    var exoPlayer:ExoPlayer?=null
    var navigationItems = listOf<BottomNavItem>()
    lateinit var navHostController:NavHostController
    var savedTheme:String?=null
    var savedSpeed: Speed=Speed.METER
    var savedLanguage:String=Locale.getDefault().language
    var savedDegrees:Degrees=Degrees.CELSIUS
    var savedMode:String=Constants.LOCATION.value
    var savedLat:Float=0f
    var savedLan:Float=0f
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navigationItems= listOf(
            BottomNavItem(
                icon = R.drawable.home,
                route = ScreenRoute.HomeScreen,
                name = getString(R.string.home)
            ),
            BottomNavItem(
                icon = R.drawable.locations,
                route = ScreenRoute.PlacesScreen,
                name = getString(R.string.locations)
            ),
            BottomNavItem(
                icon = R.drawable.bell,
                route = ScreenRoute.NotificationScreen,
                name = getString(R.string.notifications)
            ),
            BottomNavItem(
                icon = R.drawable.settings,
                route = ScreenRoute.SettingsScreen,
                name = getString(R.string.settings)
            )
        )
        val homeViewModel = ViewModelProvider(
            this, HomeViewModelFactory(
                WeatherRepositoryImp(
                    LocalDataSourceImp.getInstance(this),
                    RemoteDataSourceImp.getInstance()
                )
            )
        )[HomeViewModel::class.java]
        val notificationViewModel = ViewModelProvider(
            this, NotificationsViewModelFactory(
                WeatherRepositoryImp(
                    LocalDataSourceImp.getInstance(this),
                    RemoteDataSourceImp.getInstance()
                )
            )
        )[NotificationsViewModel::class.java]
        val homeMapViewModel = ViewModelProvider(
            this, HomeMapViewModelFactory(
                WeatherRepositoryImp(
                    LocalDataSourceImp.getInstance(this),
                    RemoteDataSourceImp.getInstance()
                )
            )
        )[HomeMapViewModel::class.java]
        val locationsViewModel = ViewModelProvider(
            this, LocationsViewModelFactory(
                WeatherRepositoryImp(
                    LocalDataSourceImp.getInstance(this),
                    RemoteDataSourceImp.getInstance()
                )
            )
        )[LocationsViewModel::class.java]
        val detailsViewModel = ViewModelProvider(
            this, DetailsViewModelFactory(
                WeatherRepositoryImp(
                    LocalDataSourceImp.getInstance(this),
                    RemoteDataSourceImp.getInstance()
                )
            )
        )[DetailsViewModel::class.java]
        val mapViewModel = ViewModelProvider(
            this, MapsViewModelFactory(
                WeatherRepositoryImp(
                    LocalDataSourceImp.getInstance(this),
                    RemoteDataSourceImp.getInstance()
                )
            )
        )[MapsViewModel::class.java]
        getSaved()
        enableEdgeToEdge()
        installSplashScreen()
        setContent {
            Initialize()
            if (mode.value==Constants.LOCATION.value)
                orderLocation()
            WeatherAppTheme(darkTheme = isNight.value) {
                Scaffold(
                    bottomBar = {NavBar()}
                ) {
                    DisplayVideo()
                    NavHost(
                        navController = navHostController,
                        startDestination = ScreenRoute.HomeScreen,
                        modifier = Modifier
                            .padding(it)
                            .fillMaxSize()
                            .background(color = Color.Transparent)
                    ){
                        composable<ScreenRoute.HomeScreen> {
                            selectedNavigationIndex.intValue=0
                            HomePage(
                                homeViewModel,
                                degrees = degrees.value,
                                speed = speed.value,
                                isNight = isNight.value,
                                info = weatherDto.value,
                            )
                        }
                        composable<ScreenRoute.PlacesScreen> {
                            selectedNavigationIndex.intValue=1
                            LocationsPage(locationsViewModel, toDetails = {
                                navHostController.navigate(ScreenRoute.DetailsScreen(it.lat,it.lon))
                            }) {
                                navHostController.navigate(ScreenRoute.MapScreen)
                            }
                        }
                        composable<ScreenRoute.MapScreen> {
                            MapsPage(mapsViewModel = mapViewModel) {
                                navHostController.popBackStack()
                            }
                        }
                        composable<ScreenRoute.DetailsScreen> {backStackEntry->
                            val value = backStackEntry.toRoute<ScreenRoute.DetailsScreen>()
                            DetailsPage(viewModel = detailsViewModel, degrees = degrees.value, speed = speed.value, isNight = isNight.value, info = WeatherDto(value.lat,value.lon)) {
                                navHostController.popBackStack()
                            }
                        }
                        composable<ScreenRoute.NotificationScreen>{
                            selectedNavigationIndex.intValue=2
                            NotificationsPage(notificationViewModel)
                        }
                        composable<ScreenRoute.HomeMapScreen>{
                            HomeMapPage(homeMapViewModel) {
                                val editor=getSharedPreferences("saved",Context.MODE_PRIVATE).edit()
                                editor.putFloat(Constants.LAT.value,it.lat.toFloat())
                                editor.putFloat(Constants.LAN.value,it.long.toFloat())
                                weatherDto.value= WeatherDto(it.lat,it.long)
                                editor.apply()
                                navHostController.popBackStack()
                            }
                        }
                        composable<ScreenRoute.SettingsScreen>{
                            selectedNavigationIndex.intValue=3
                            SettingsPage(
                                isNight = isNight.value,
                                language = language.value,
                                speed = speed.value,
                                degrees = degrees.value,
                                mode = mode.value,
                                changeLanguage = {
                                    val editor=getSharedPreferences("saved",Context.MODE_PRIVATE).edit()
                                    language.value=it
                                    editor.putString(Constants.LANG.value,it)
                                    editor.apply()
                                    setAppLocale(it)
                                    this@MainActivity.recreate()

                                },
                                changeDegrees = {
                                    val editor=getSharedPreferences("saved",Context.MODE_PRIVATE).edit()
                                    degrees.value=it
                                    editor.putInt(Constants.DEGREES.value,it.ordinal)
                                    editor.apply()
                                },
                                changeSpeed = {
                                    val editor=getSharedPreferences("saved",Context.MODE_PRIVATE).edit()
                                    speed.value=it
                                    editor.putInt(Constants.SPEED.value,it.ordinal)
                                    editor.apply()
                                },
                                changeMode = {
                                    val editor=getSharedPreferences("saved",Context.MODE_PRIVATE).edit()
                                    mode.value=it
                                    editor.putString(Constants.MODE.value,it)
                                    if(mode.value==Constants.MAP.value){
                                        editor.apply()
                                        navHostController.navigate(ScreenRoute.HomeMapScreen)
                                    }
                                    else{
                                        editor.remove(Constants.LAT.value)
                                        editor.remove(Constants.LAN.value)
                                        editor.apply()
                                    }
                                },
                                changeColor = {
                                    val editor=getSharedPreferences("saved",Context.MODE_PRIVATE).edit()
                                    isNight.value=it
                                    editor.putString(Constants.COLORS.value,if(it) Constants.DARK.value else Constants.LIGHT.value)
                                    editor.apply()
                                    this@MainActivity.recreate()
                                },
                                innerPaddingValues = PaddingValues(0.dp)
                            )
                        }

                    }
                }

            }
        }
    }

    private fun getSaved(){
        val sharedPreferences=getSharedPreferences("saved",Context.MODE_PRIVATE)
        val editor=sharedPreferences.edit()
        savedTheme=sharedPreferences.getString(Constants.COLORS.value,null)
        val lang=sharedPreferences.getString(Constants.LANG.value,null)
        val spe=sharedPreferences.getInt(Constants.SPEED.value,-1)
        val deg=sharedPreferences.getInt(Constants.DEGREES.value,-1)
        val mode=sharedPreferences.getString(Constants.MODE.value,null)
        if (lang==null){
            editor.putString(Constants.LANG.value,Locale.getDefault().language)
            savedLanguage=Locale.getDefault().language
        }
        else{
            savedLanguage=lang
            setAppLocale(savedLanguage)
        }

        if (spe==-1){
            editor.putInt(Constants.SPEED.value,Speed.METER.ordinal)
            savedSpeed=Speed.METER
        }
        else{
            savedSpeed=Speed.entries[spe]
        }

        if (deg==-1){
            editor.putInt(Constants.DEGREES.value,Degrees.CELSIUS.ordinal)
            savedDegrees=Degrees.CELSIUS
        }
        else{
            savedDegrees=Degrees.entries[deg]
        }
        if (mode==null){
            editor.putString(Constants.MODE.value,Constants.LOCATION.value)
            savedMode=Constants.LOCATION.value
        }
        else{
            savedMode=mode
            if (mode==Constants.MAP.value){
                savedLan=sharedPreferences.getFloat(Constants.LAN.value,0.0f)
                savedLat=sharedPreferences.getFloat(Constants.LAT.value,0.0f)
            }
        }
        editor.apply()

    }

    @Composable
    private fun Initialize() {
        var night=isSystemInDarkTheme()
        weatherDto = remember {
            mutableStateOf(WeatherDto(savedLat.toDouble(), savedLan.toDouble()))
        }

        if (savedTheme==null){
            val editor=getSharedPreferences("saved",Context.MODE_PRIVATE).edit()
            savedTheme=if(night) Constants.DARK.value else Constants.LIGHT.value
            editor.putString(Constants.COLORS.value, savedTheme)
            editor.apply()
        }
        else{
            night = savedTheme==Constants.DARK.value
        }
        isNight = rememberSaveable {
            mutableStateOf(night)
        }
        speed = rememberSaveable {
            mutableStateOf(savedSpeed)
        }
        degrees = rememberSaveable {
            mutableStateOf(savedDegrees)
        }
        language= rememberSaveable{
            mutableStateOf(savedLanguage)
        }
        mode= rememberSaveable{
            mutableStateOf(savedMode)
        }
        navHostController= rememberNavController()
        exoPlayer = remember {
            buildExoPlayer(
                if (isNight.value) getNightVideoUri(packageName) else getDayVideoUri(packageName)
            )
        }
    }

    private fun setAppLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = resources.configuration
        config.setLocale(locale)
        config.setLayoutDirection(locale)

        createConfigurationContext(config)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    private fun Context.buildExoPlayer(uri: Uri) =
        ExoPlayer.Builder(this).build().apply {
            setMediaItem(MediaItem.fromUri(uri))
            repeatMode = Player.REPEAT_MODE_ALL
            playWhenReady = true
            volume=0f
            prepare()
        }

    @OptIn(UnstableApi::class)
    fun Context.buildPlayerView(exoPlayer: ExoPlayer) =
        PlayerView(this).apply {
            player = exoPlayer
            layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
            useController = false
            resizeMode = RESIZE_MODE_ZOOM
        }

    @Composable
    private fun DisplayVideo() {
        DisposableEffect(
            AndroidView(
                factory = { it.buildPlayerView(exoPlayer!!) },
                modifier = Modifier.fillMaxSize()
            )
        ) {
            onDispose {
                exoPlayer?.release()
            }
        }
    }

    @Composable
    private fun NavBar(){
        selectedNavigationIndex = rememberSaveable {
            mutableIntStateOf(0)
        }
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
        ){
            navigationItems.forEachIndexed { index, item ->
                NavigationBarItem(
                    selected = selectedNavigationIndex.intValue==index,
                    onClick = {
                        if (index!=selectedNavigationIndex.intValue) {
                            navHostController.navigate(item.route)
                        }
                        if (selectedNavigationIndex.intValue==0){
                            if (mode.value==Constants.LOCATION.value)
                                orderLocation()
                        }
                    },
                    icon = {
                        Icon(
                            painter = painterResource(item.icon),
                            contentDescription = item.name,
                        )
                    },
                    label = {
                        if(index==selectedNavigationIndex.intValue){
                            Text(".")
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = MaterialTheme.colorScheme.onPrimary,
                        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                        indicatorColor = Color.Transparent,
                    )
                )
            }
        }
    }

    private fun getDayVideoUri(packageName:String): Uri {
        val rawId = R.raw.day
        val videoUri = "android.resource://$packageName/$rawId"
        return Uri.parse(videoUri)
    }

    private fun getNightVideoUri(packageName:String): Uri {
        val rawId = R.raw.night
        val videoUri = "android.resource://$packageName/$rawId"
        return Uri.parse(videoUri)
    }

    override fun onStart() {
        super.onStart()
        exoPlayer?.prepare()
    }

    private fun orderLocation() {
        if (checkPermission()) {
            if (isLocationEnabled()) {
                getFreshLocation()
            } else {
                enableLocationServices()
            }

        } else {
            this.requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                REQUEST_LOCATION_CODE
            )
        }
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)} passing\n      in a {@link RequestMultiplePermissions} object for the {@link ActivityResultContract} and\n      handling the result in the {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_CODE) {
            if (grantResults.get(0) == PackageManager.PERMISSION_GRANTED || grantResults.get(1) == PackageManager.PERMISSION_GRANTED) {
                if (isLocationEnabled()) {
                    getFreshLocation()
                } else {
                    enableLocationServices()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
        deviceId: Int
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId)
        if (requestCode == REQUEST_LOCATION_CODE) {
            if (grantResults.get(0) == PackageManager.PERMISSION_GRANTED || grantResults.get(1) == PackageManager.PERMISSION_GRANTED) {
                if (isLocationEnabled()) {
                    getFreshLocation()
                } else {
                    enableLocationServices()
                }
            }
        }
    }

    private fun checkPermission(): Boolean {
        return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun isLocationEnabled(): Boolean {
        val manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER) || manager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun enableLocationServices() {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        startActivity(intent)
    }

    @SuppressLint("MissingPermission")
    fun getFreshLocation() {
        fusedProvider = LocationServices.getFusedLocationProviderClient(this)
        fusedProvider.requestLocationUpdates(
            LocationRequest.Builder(0).apply {
                setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            }.build(),
            object : LocationCallback() {

                override fun onLocationResult(result: LocationResult) {
                    super.onLocationResult(result)
                    val location = result.lastLocation ?: Location("")
                    weatherDto.value = WeatherDto(location.latitude, location.longitude)
                    fusedProvider.removeLocationUpdates(this)
                }
            },
            Looper.myLooper()
        )
    }

    override fun onStop() {
        super.onStop()
        exoPlayer?.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer?.release()
    }


}
