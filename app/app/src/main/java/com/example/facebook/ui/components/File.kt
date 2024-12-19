package com.example.facebook.ui.components

import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil3.compose.SubcomposeAsyncImage
import com.example.facebook.R.drawable
import com.example.facebook.util.toTime

@Composable
fun File(
    id: String,
    modifier: Modifier = Modifier,
    fileViewModel: FileViewModel = viewModel(factory = FileViewModel.Factory),
    allowOrigin: Boolean = true
) {
    val file = fileViewModel.getFileById(id).collectAsState().value
    var view by remember { mutableStateOf(false) }

    Box(modifier = modifier
        .wrapContentSize()
        .testTag("BoxFile")) {

        if (file == null || file.url.isEmpty()) {
            CircularProgressIndicator()
            return
        }

        when (file.type) {
            "image" -> {
                if (file.status == "safe" || view) {
                    SubcomposeAsyncImage(
                        model = file.url,
                        contentDescription = file.name,
                        contentScale = ContentScale.Crop,
                        loading = { CircularProgressIndicator() },
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("ImageFile")
                    )
                } else if (file.status == "unsafe") {
                    SubcomposeAsyncImage(
                        model = file.blurUrl,
                        contentDescription = file.name,
                        contentScale = ContentScale.Crop,
                        loading = { CircularProgressIndicator() },
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("UnsafeFile")
                    )
                    if (allowOrigin) {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("This image has been flagged as inappropriate.")
                            Button(onClick = { view = true }) {
                                Text("View anyway")
                            }
                        }
                    }
                } else if (file.status == "processing") {
                    SubcomposeAsyncImage(
                        model = file.blurUrl,
                        contentDescription = file.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("ProcessingFile")
                    )
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else {
                    CircularProgressIndicator()
                }
            }

            "video" -> {
                VideoFile(file.url)
                Modifier.testTag("VideoFile")
            }

            "audio" -> {
                AudioFile(Uri.parse(file.url), file.description)
                Modifier.testTag("AudioFile")
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
        }, modifier = Modifier.fillMaxSize()
    )

    DisposableEffect(Unit) {

        onDispose {
            exoPlayer.release()
        }
    }
}

@Composable
fun AudioFile(uri: Uri, description: String) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var duration by remember { mutableLongStateOf(1000) }
    var currentPosition by remember { mutableLongStateOf(0) }
    val handler = remember { Handler(Looper.getMainLooper()) }

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(uri))
            prepare()
        }
    }

    val onCardClick = {
        if (isPlaying) {
            isPlaying = false
            exoPlayer.pause()
        } else {
            isPlaying = true
            if (exoPlayer.currentPosition >= exoPlayer.duration) {
                exoPlayer.seekTo(0)
            }
            exoPlayer.play()
        }
    }

    DisposableEffect(Unit) {
        val updatePositionRunnable = object : Runnable {
            override fun run() {
                currentPosition = exoPlayer.currentPosition / 1000
                handler.postDelayed(this, 1000) // Update every second
            }
        }

        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                duration = (exoPlayer.duration / 1000).coerceAtLeast(1)
            }

            override fun onIsPlayingChanged(a: Boolean) {
                isPlaying = a
                currentPosition = exoPlayer.currentPosition / 1000
                if (a) {
                    handler.post(updatePositionRunnable)
                } else {
                    handler.removeCallbacks(updatePositionRunnable)
                }
            }
        }

        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
            handler.removeCallbacks(updatePositionRunnable)
            exoPlayer.release()
        }
    }

    Card(
        onClick = onCardClick, colors = CardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            disabledContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            disabledContainerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(painterResource(drawable.baseline_mic_24), contentDescription = "Mic")
                Text("${currentPosition.toTime()} / ${duration.toTime()}")
                if (isPlaying) Icon(
                    painterResource(drawable.baseline_pause_24),
                    contentDescription = "Pause"
                )
                else Icon(
                    Icons.Default.PlayArrow, contentDescription = "PlayArrow"
                )
            }
            Slider(
                value = currentPosition.toFloat(),
                onValueChange = {
                    exoPlayer.play()
                    exoPlayer.seekTo((it * 1000).toLong())
                },
                valueRange = 0f..duration.toFloat(),
                steps = 100
            )
            if (description.isNotBlank()) {
                Text(description)
            }
        }
    }
}