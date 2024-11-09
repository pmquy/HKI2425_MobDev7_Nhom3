package com.example.facebook.ui.components

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.example.facebook.R

@Composable
fun File(
    id: String,
    modifier: Modifier = Modifier,
    fileViewModel: FileViewModel = viewModel(factory = FileViewModel.Factory)
) {
    val state = fileViewModel.getFileById(id).collectAsState()

    Box(modifier = modifier) {

        when (state.value.type) {
            "image" -> {
                if(state.value.status == "safe") {
                    AsyncImage(
                        model = state.value.url,
                        contentDescription = state.value.name,
                        contentScale = ContentScale.Crop
                    )
                } else if(state.value.status == "unsafe") {
                    AsyncImage(
                        model = state.value.blurUrl,
                        contentDescription = state.value.name,
                        contentScale = ContentScale.Crop
                    )
                } else {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.Center)
                    )
                    AsyncImage(
                        model = state.value.blurUrl,
                        contentDescription = state.value.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            "video" -> {
                if (state.value.url.isNotEmpty())
                    VideoFile(state.value.url)
            }

            "audio" -> {
                if (state.value.url.isNotEmpty())
                    AudioFile(state.value.url)
            }

            else -> {
                Text("Unknown file type")
            }
        }
    }
}


@Composable
fun VideoFile(url: String) {
    val context = LocalContext.current

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(Uri.parse(url)))
            prepare()
        }
    }

    AndroidView(
        factory = {
            PlayerView(it).apply {
                player = exoPlayer
            }
        },
        modifier = Modifier.fillMaxSize()
    )

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }
}

@Composable
fun AudioFile(url: String) {
    val context = LocalContext.current

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(Uri.parse(url)))
            prepare()
        }
    }

    Card(
        onClick = { if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play() },
        colors = CardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            disabledContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            disabledContainerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(Modifier.padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(painterResource(R.drawable.baseline_mic_24), contentDescription = "")
            Text("00:00 / 01:00")
            Icon(Icons.Default.PlayArrow, contentDescription = "")
        }
    }


    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }
}
