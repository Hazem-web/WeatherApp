package com.example.weatherapp

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weatherapp.data.models.ForecastWeatherResponse
import com.example.weatherapp.data.models.Results
import com.example.weatherapp.data.models.Weather
import com.example.weatherapp.data.models.WeatherDto
import com.example.weatherapp.data.models.WeatherResponse
import com.example.weatherapp.ui.theme.TransparentWhite
import com.example.weatherapp.ui.theme.WeatherAppTheme
import com.example.weatherapp.viewmodels.HomeViewModel
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
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
    object DetailsScreen:ScreenRoute()

    @Serializable
    object MapScreen:ScreenRoute()
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
        viewModel.getAll(info.lat,info.lon,info.lang)
        val weatherState = viewModel.weather.collectAsState()
        val forecastState= viewModel.forecast.collectAsState()
        val scope = rememberCoroutineScope()
        val snackBarHostState = remember { SnackbarHostState() }
        Scaffold(
            snackbarHost = { SnackbarHost(snackBarHostState) },
            modifier = Modifier.fillMaxWidth().background(color = Color.Transparent),
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
                                message = "Error: ${(weatherState.value as Results.Failure).error.message} ",
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
                                message = "Error: ${(forecastState.value as Results.Failure).error.message} ",
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
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
        modifier = Modifier.background(color = Color.Transparent).padding(horizontal = 20.dp),
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
    val todayWeightState= remember { mutableStateOf(FontWeight.Bold) }
    val daysWeightState= remember { mutableStateOf(FontWeight.Normal) }
    Column(
        modifier = Modifier.background(color = Color.Transparent).fillMaxWidth(),
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

@Preview(showBackground = true)
@Composable
fun ForecastItemPreview(){
    WeatherAppTheme {
        ForecastItem("08:00", R.drawable.bit_cloudy, "bit cloudy", "13°")
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

