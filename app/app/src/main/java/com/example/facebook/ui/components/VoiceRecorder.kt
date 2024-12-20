package com.example.facebook.ui.components

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.facebook.R
import kotlinx.coroutines.delay
import java.io.File
import java.io.FileOutputStream

private enum class RecorderState {
    START,
    RECORDING,
    PAUSED,
    DONE
}


@Composable
fun VoiceRecorder(modifier: Modifier = Modifier, onDone: (Pair<File, String>?) -> Unit) {

    val context = LocalContext.current
    var granted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        granted = it
    }

    when {
        granted -> {
            var state by remember { mutableStateOf(RecorderState.START) }
            var recorder: MediaRecorder? by remember { mutableStateOf(null) }
            var file: File? by remember { mutableStateOf(null) }
            var timeCount by remember { mutableLongStateOf(0) }

            LaunchedEffect(state, timeCount) {
                if (state == RecorderState.RECORDING) {
                    delay(100)
                    timeCount += 100
                }
            }

            val onStartRecorder: () -> Unit = {
                file = File(context.filesDir, "audio.mp3")
                file?.let {
                    recorder = MediaRecorder().apply {
                        setAudioSource(MediaRecorder.AudioSource.MIC)
                        setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                        setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                        setOutputFile(FileOutputStream(it).fd)
                        try {
                            prepare()
                        } catch (e: Exception) {
                            Log.e("Recorder", "prepare() failed ${e.message}")
                        }
                        start()
                    }
                    state = RecorderState.RECORDING
                    timeCount = 0
                }
            }

            val onPauseRecorder: () -> Unit = {
                recorder?.apply {
                    pause()
                    state = RecorderState.PAUSED
                }
            }

            val onResumeRecorder: () -> Unit = {
                recorder?.apply {
                    resume()
                    state = RecorderState.RECORDING
                }
            }

            val onStopRecorder: () -> Unit = {
                recorder?.apply {
                    stop()
                    release()
                    state = RecorderState.DONE
                    onDone(Pair(file!!, "audio/mp3"))
                }
            }

            val onRestartRecorder = {
                state = RecorderState.START
                onDone(null)
            }

            Column(
                modifier = modifier.verticalScroll(rememberScrollState())
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (state != RecorderState.START && state != RecorderState.DONE) {
                        IconButton(onRestartRecorder) {
                            Icon(Icons.Default.Refresh, contentDescription = "")
                        }
                    }

                    Spacer(modifier = Modifier.width(50.dp))

                    when (state) {
                        RecorderState.START, RecorderState.DONE -> {
                            IconButton(
                                onStartRecorder,
                                colors = IconButtonDefaults.filledIconButtonColors()
                            ) {
                                Icon(
                                    painterResource(R.drawable.baseline_mic_24),
                                    contentDescription = ""
                                )
                            }
                        }

                        RecorderState.RECORDING -> {
                            IconButton(
                                onPauseRecorder,
                                colors = IconButtonDefaults.filledIconButtonColors()
                            ) {
                                Icon(
                                    painterResource(R.drawable.baseline_pause_24),
                                    contentDescription = ""
                                )
                            }
                        }

                        RecorderState.PAUSED -> {
                            IconButton(
                                onResumeRecorder,
                                colors = IconButtonDefaults.filledIconButtonColors()
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(50.dp))

                    if (state != RecorderState.START && state != RecorderState.DONE) {
                        IconButton(onStopRecorder) {
                            Icon(
                                painterResource(R.drawable.baseline_stop_24),
                                contentDescription = ""
                            )
                        }
                    }
                }

                Text(text = "Time: ${timeCount / 1000f} seconds")

                if (state == RecorderState.DONE) {
                    AudioFile(Uri.fromFile(file!!), "")
                }
            }
        }

        ActivityCompat.shouldShowRequestPermissionRationale(
            context as Activity, Manifest.permission.RECORD_AUDIO
        ) -> {
            Text("Permission is needed to record audio")
            Button(
                onClick = {
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            ) {
                Text("Grant Permission")
            }
        }

        else -> {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

}