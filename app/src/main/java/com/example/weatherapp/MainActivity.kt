package com.example.weatherapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
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
import com.example.weatherapp.data.local.LocalDataSourceImp
import com.example.weatherapp.data.models.BottomNavItem
import com.example.weatherapp.data.models.WeatherDto
import com.example.weatherapp.data.remote.RemoteDataSourceImp
import com.example.weatherapp.data.repo.WeatherRepositoryImp
import com.example.weatherapp.ui.theme.WeatherAppTheme
import com.example.weatherapp.viewmodels.HomeViewModel
import com.example.weatherapp.viewmodels.HomeViewModelFactory
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

const val REQUEST_LOCATION_CODE=1003

class MainActivity : ComponentActivity() {
    lateinit var fusedProvider: FusedLocationProviderClient
    lateinit var weatherDto: MutableState<WeatherDto>
    lateinit var isNight: MutableState<Boolean>
    lateinit var speed: MutableState<Speed>
    lateinit var degrees: MutableState<Degrees>
    var exoPlayer:ExoPlayer?=null
    var navigationItems = listOf<BottomNavItem>()
    lateinit var navHostController:NavHostController
    override fun onCreate(savedInstanceState: Bundle?) {
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
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val homeViewModel = ViewModelProvider(
                this, HomeViewModelFactory(
                    WeatherRepositoryImp(
                        LocalDataSourceImp.getInstance(this),
                        RemoteDataSourceImp.getInstance()
                    )
                )
            )[HomeViewModel::class.java]
            Initialize()
            WeatherAppTheme(darkTheme = isNight.value) {
                Scaffold(
                    bottomBar = {NavBar()}
                ) {
                    DisplayVideo()
                    NavHost(
                        navController = navHostController,
                        startDestination = ScreenRoute.HomeScreen,
                        modifier = Modifier.padding(it).fillMaxSize().background(color = Color.Transparent)
                    ){
                        composable<ScreenRoute.HomeScreen> {
                            orderLocation()
                            HomePage(
                                homeViewModel,
                                degrees = degrees.value,
                                speed = speed.value,
                                isNight = isNight.value,
                                info = weatherDto.value,
                            )
                        }
                        composable<ScreenRoute.PlacesScreen> {
                            Text("hi")
                        }
                    }
                }

            }
        }
    }

    @Composable
    private fun Initialize() {

        weatherDto = remember {
            mutableStateOf(WeatherDto(0.0, 0.0, "en"))
        }
        isNight = rememberSaveable {
            mutableStateOf(true)
        }
        speed = rememberSaveable {
            mutableStateOf(Speed.METER)
        }
        degrees = rememberSaveable {
            mutableStateOf(Degrees.CELSIUS)
        }
        isNight.value = isSystemInDarkTheme()
        navHostController= rememberNavController()
        exoPlayer = remember {
            buildExoPlayer(
                if (isNight.value) getNightVideoUri(packageName) else getDayVideoUri(packageName)
            )
        }
    }

    fun Context.buildExoPlayer(uri: Uri) =
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
        val selectedNavigationIndex = rememberSaveable {
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
                            selectedNavigationIndex.intValue = index
                            navHostController.navigate(item.route)
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
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                REQUEST_LOCATION_CODE
            )
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
                    weatherDto.value = WeatherDto(location.latitude, location.longitude, weatherDto.value.lang)
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
