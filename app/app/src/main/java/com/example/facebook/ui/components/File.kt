package com.example.facebook.ui.components

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.facebook.R.drawable

@Composable
fun File(
    id: String,
    modifier: Modifier = Modifier,
    fileViewModel: FileViewModel = viewModel(factory = FileViewModel.Factory)
) {
    val file = fileViewModel.getFileById(id).collectAsState().value
    var view by remember { mutableStateOf(false) }

    Box(modifier = modifier) {

        when (file?.type) {
            "image" -> {
                if(file.status == "safe" || view) {
                    AsyncImage(
                        model = file.url,
                        contentDescription = file.name,
                        contentScale = ContentScale.Crop,
                    )
                } else if(file.status == "unsafe") {
                    AsyncImage(
                        model = file.blurUrl,
                        contentDescription = file.name,
                        contentScale = ContentScale.Crop,
                    )
                    Column (modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("This image has been flagged as inappropriate.")
                        Button( onClick = {view = true}) {
                            Text("View anyway")
                        }
                    }
                } else {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.Center)
                    )
                    AsyncImage(
                        model = file.blurUrl,
                        contentDescription = file.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            "video" -> {
                if (file.url.isNotEmpty())
                    VideoFile(file.url)
            }

            "audio" -> {
                Column {
                    if (file.url.isNotEmpty())
                        AudioFile(file.url)
                    Text(file.description)
                }
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
            Icon(painterResource(drawable.baseline_mic_24), contentDescription = "")
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
