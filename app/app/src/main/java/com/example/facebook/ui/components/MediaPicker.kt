package com.example.facebook.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog


@Composable
fun MediaPicker(onDone: (List<Uri>) -> Unit) {

    var result by remember { mutableStateOf(emptyList<Uri>()) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) {
        result += it
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

            ){
                Column(
                    modifier = Modifier
                        .sizeIn(
                            minWidth = 250.dp,
                            maxWidth = 400.dp,
                            minHeight = 250.dp,
                            maxHeight = 400.dp
                        )
                        .verticalScroll(rememberScrollState()),
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
                    result.forEach {
                        Text(it.toString())
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
