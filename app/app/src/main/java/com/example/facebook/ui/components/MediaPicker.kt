package com.example.facebook.ui.components

import android.annotation.SuppressLint
import android.content.ContentValues
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.facebook.util.uriToFile
import java.io.File
import java.io.FileOutputStream


@SuppressLint("Recycle")
@Composable
fun Recorder(onDone: (Pair<File, String>) -> Unit) {

    val context = LocalContext.current
    var recording by remember { mutableStateOf(false) }
    var recorder: MediaRecorder? by remember { mutableStateOf(null) }
    var player: MediaPlayer? by remember { mutableStateOf(null) }
    var file: File? by remember { mutableStateOf(null) }

    val onStartRecording: () -> Unit = {

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
            Log.d("URI", it.toString())
            recording = true
        }
    }

    val onStopRecording = {
        recorder?.apply {
            stop()
            release()
        }
        recording = false
        onDone(Pair(file!!, "audio/mp3"))
    }

    val onPlay: () -> Unit = {
        player = MediaPlayer.create(context, Uri.fromFile(file)).apply {
            start()
        }
    }

    val onStop: () -> Unit = {
        player?.apply {
            stop()
            release()
        }
    }

    Column(
        modifier = Modifier
            .sizeIn(minHeight = 50.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Button(
            if (recording) onStopRecording else onStartRecording,
            modifier = Modifier.sizeIn(minWidth = 100.dp)
        ) {
            Text(if (recording) "Stop" else "Record")
        }
        Button(
            if (player?.isPlaying == true) onStop else onPlay,
            modifier = Modifier.sizeIn(minWidth = 100.dp)
        ) {
            Text(if (player?.isPlaying == true) "Stop" else "Play")
        }
    }


}

@Composable
fun MediaPicker(onDone: (List<Pair<File, String>>) -> Unit) {

    var result by remember { mutableStateOf(emptyList<Pair<File, String>>()) }
    val context = LocalContext.current

    val filePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) {
        result += it.map { uriToFile(context, it) }
    }

    var open by remember { mutableStateOf(false) }

    IconButton({ open = true }) {
        Icon(Icons.Default.Add, contentDescription = "Add media")
    }

    if (open) {
        Dialog(
            onDismissRequest = { open = false }
        ) {
            Card(

            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .sizeIn(
                            minWidth = 250.dp,
                            minHeight = 250.dp,
                            maxHeight = 500.dp,
                        ),
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                ) {
                    Button({ filePickerLauncher.launch("image/*") }) {
                        Text("Pick Image")
                    }
                    Button({ filePickerLauncher.launch("video/*") }) {
                        Text("Pick Video")
                    }
                    Button({ filePickerLauncher.launch("audio/*") }) {
                        Text("Pick Audio")
                    }
                    Button({ filePickerLauncher.launch("*/*") }) {
                        Text("Pick File")
                    }
                    Recorder { result += it }

                    result.forEach {
                            Text(it.first.name)
                            IconButton({ result -= it }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove")
                            }
                    }
                    Button({
                        open = false
                        onDone(result)
                    }) {
                        Text("Done")
                    }
                }
            }
        }
    }

}
