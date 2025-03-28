package com.example.weatherapp

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
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
import com.example.weatherapp.data.models.Weather
import com.example.weatherapp.data.models.WeatherResponse
import com.example.weatherapp.ui.theme.TransparentWhite
import com.example.weatherapp.ui.theme.WeatherAppTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class Screens {






    @Composable
    fun HomePage(){
        Scaffold { innerPadding->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(15.dp),
                horizontalAlignment = Alignment.CenterHorizontally
                ) {

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

fun getIcon(weather: Weather, isNight:Boolean): Int {
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
fun WeatherDetails(weatherResponse: WeatherResponse, degrees: Degrees, isNight: Boolean){
    val calendar = Calendar.getInstance()
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    Column(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CountryName(weatherResponse.name,weatherResponse.place.country)
        calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault())
            ?.let {
                Text(
                    text = "$it - ${formatter.format(calendar.time)}"
                )
            }
        Image(
            painter = painterResource(id = getIcon(
                weatherResponse.weather[0],
                isNight
            )),
            contentDescription = weatherResponse.weather[0].description + stringResource(R.string.icon),
            modifier = Modifier
                .width(138.dp)
                .height(91.dp),
        )
        TemperatureString(
            temp = weatherResponse.details.temp,
            degrees = degrees,
            fontWeight = FontWeight.Bold,
            fontSize = 48.sp
        )
        TemperatureDetails(
            min = weatherResponse.details.min,
            max = weatherResponse.details.max,
            feels = weatherResponse.details.feels,
            degrees = degrees
        )
        Text(
            text = weatherResponse.weather[0].description,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun WeatherInfo(weatherResponse: WeatherResponse, speed: Speed){
    WeatherAppTheme {
        Row(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(MaterialTheme.colorScheme.primary,Color.White)
                    )
                ),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ItemInfo(
                stringResource(R.string.humidity),
                "${weatherResponse.details.humidity}%",
                R.drawable.humidity_icon
            )
            ItemInfo(
                stringResource(R.string.clouds),
                "${weatherResponse.clouds.all}%",
                R.drawable.clouds_icon
            )
            ItemInfo(
                stringResource(R.string.pressure),
                "${weatherResponse.details.pressure}${stringResource(R.string.hpa)}",
                R.drawable.pressure_icon
            )
            WindInfo(weatherResponse.wind.speed, speed)
        }
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
        modifier = Modifier.width(138.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TemperatureString(
            temp = min,
            degrees = degrees,
            fontSize = 12.sp,
            fontWeight = FontWeight.Light,
            color = TransparentWhite,
            text = stringResource(R.string.weather_min)
        )
        TemperatureString(
            temp = feels,
            degrees = degrees,
            fontSize = 12.sp,
            fontWeight = FontWeight.Light,
            color = TransparentWhite,
            text = stringResource(R.string.feels_like)
        )
        TemperatureString(
            temp = max,
            degrees = degrees,
            fontSize = 12.sp,
            fontWeight = FontWeight.Light,
            color = TransparentWhite,
            text = stringResource(R.string.weather_max)
        )
    }
}

@Composable
private fun CountryName(place:String, country:String) {
    Row(
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = "$place,",
            fontWeight = FontWeight.Bold
        )
        Text(
            text = country,
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
fun ItemInfo(text: String, value: String, icon:Int){
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column() {
            Image(
                painter = painterResource(icon),
                contentDescription = text + stringResource(R.string.icon),
                modifier = Modifier.width(24.dp).height(24.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
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
                modifier = Modifier.width(24.dp).height(24.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text= stringResource(R.string.wind),
                textAlign = TextAlign.Center,
                fontSize = 10.sp,
            )
        }
        Text(text= "${getSpeed(value,speed)}$speedValue")
    }
}

@Composable
fun ForecastItem(date:String, icon: Int, desc:String, degree:String){
    WeatherAppTheme {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.secondary)
                .padding(horizontal = 5.dp, vertical = 3.dp)
                .clip(RoundedCornerShape(10.dp)),
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
}

@Composable
fun ForecastDetails(forecast: ForecastWeatherResponse, degrees: Degrees,isNight: Boolean){
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_YEAR, 1)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val tomorrow=  calendar.time.time
    val forecastListState= remember {
        val today=forecast.forecastList.filter {
            it.dt*1000<tomorrow
        }
        mutableStateOf(today)
    }
    val todayWeightState= remember { mutableStateOf(FontWeight.Bold) }
    val daysWeightState= remember { mutableStateOf(FontWeight.Normal) }
    Column(
        modifier = Modifier.fillMaxWidth(),
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
                    forecastListState.value=forecast.forecastList.filter {
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
                    forecastListState.value=forecast.forecastList.filter {
                        it.dt*1000>=tomorrow && (it.dt*1000%129600000)== 0.toLong()
                    }
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
            items(forecastListState.value.size){
                val item= forecastListState.value[it]
                val date=
                if (daysWeightState.value==FontWeight.Bold){
                    getDayOfWeekOld(item.dt*1000)
                }
                else{
                     getTimeOld(item.dt*1000)
                }
                ForecastItem(
                    date = date,
                    icon = getIcon(
                        weather = item.weather[0],
                        isNight
                    ),
                    desc = item.weather[0].description,
                    degree = "${getTemp( temp = item.details.temp, degrees = degrees)}°"
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

@Composable
fun BorderedRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .padding(16.dp)
            .drawBehind {
                val strokeWidth = 2.dp.toPx() // Border thickness
                drawLine(
                    color = Color.Black, // Border color
                    start = androidx.compose.ui.geometry.Offset(0f, size.height), // Start from left bottom
                    end = androidx.compose.ui.geometry.Offset(size.width, size.height), // End at right bottom
                    strokeWidth = strokeWidth
                )
            },
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = "Item 1")
        Text(text = "Item 2")
        Text(text = "Item 3")
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

@Preview
@Composable
fun PreviewBorderedRow() {
    BorderedRow()
}