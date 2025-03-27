package com.example.weatherapp

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM
import androidx.media3.ui.PlayerView
import com.example.weatherapp.ui.theme.WeatherAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WeatherAppTheme {

                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(),
                        if(!isSystemInDarkTheme()) getDayVideoUri() else getNightVideoUri()
                    )
            }
        }
    }
    private fun getDayVideoUri(): Uri {
        val rawId = R.raw.day
        val videoUri = "android.resource://$packageName/$rawId"
        return Uri.parse(videoUri)
    }

    private fun getNightVideoUri(): Uri {
        val rawId = R.raw.night
        val videoUri = "android.resource://$packageName/$rawId"
        return Uri.parse(videoUri)
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
fun Greeting(name: String, modifier: Modifier = Modifier, videoUri:Uri) {

    val context = LocalContext.current
    val exoPlayer = remember { context.buildExoPlayer(videoUri) }
    DisposableEffect(
        AndroidView(
            factory = { it.buildPlayerView(exoPlayer) },
            modifier = Modifier.fillMaxSize()
        )
    ) {
        onDispose {
            exoPlayer.release()
        }
    }

    Text(
        text = "Hello $name!",
        modifier = modifier.fillMaxSize()
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    WeatherAppTheme {

    }
}