package com.example.facebook.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickMultipleVisualMedia
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.facebook.util.uriToFile
import java.io.File

@Composable
fun ImagePicker(onImageSelected: (Pair<File, String>) -> Unit) {

    val context = LocalContext.current

    val imageLauncher = rememberLauncherForActivityResult(
        PickVisualMedia()
    ) {
        if (it != null) onImageSelected(uriToFile(context, it))
    }

    Button(onClick = { imageLauncher.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly)) }) {
        Text("Chọn ảnh")
    }
}

@Composable
fun MultipleImagePicker(onImageSelected: (List<Pair<File, String>>) -> Unit) {

    val context = LocalContext.current

    val imageLauncher = rememberLauncherForActivityResult(
        PickMultipleVisualMedia()
    ) {
        onImageSelected(it.map { uri -> uriToFile(context, uri) })
    }

    Button(onClick = { imageLauncher.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly)) }) {
        Text("Chọn ảnh")
    }
}