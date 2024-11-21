package com.example.facebook.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.facebook.util.uriToFile
import java.io.File

@Composable
fun MediaPicker(modifier: Modifier = Modifier, onDone: (List<Pair<File, String>>) -> Unit) {
    val context = LocalContext.current

    val filePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) {
        onDone(it.map { uriToFile(context, it) })
    }

    Column(
        modifier,
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
    }
}