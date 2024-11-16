package com.example.facebook.ui.components

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.annotation.RequiresApi
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.facebook.util.uriToFile
import java.io.File

@RequiresApi(Build.VERSION_CODES.Q)
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