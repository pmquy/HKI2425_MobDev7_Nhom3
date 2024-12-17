package com.example.facebook.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.geometry.Offset
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.facebook.FacebookApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class ImageViewModel(
    val application: FacebookApplication
) : ViewModel() {
    private val _uiState = MutableStateFlow(ImageUIState())
    val uiState = _uiState.asStateFlow()

    fun reset() {
        _uiState.update {
            it.copy(
                scale = 1f,
                rotationState = 1f,
                offsetX = 0f,
                offsetY = 0f,
                focalX = 0f,
                focalY = 0f
            )
        }
    }

    val onGesture: (Offset, Offset, Float, Float) -> Unit = { centroid, pan, zoom, rotation ->
        _uiState.update {
            it.copy(
                scale = _uiState.value.scale * zoom,
                focalX = centroid.x,
                focalY = centroid.y,
                offsetX = (_uiState.value.offsetX + pan.x) + (centroid.x - _uiState.value.focalX) * (1 - zoom),
                offsetY = (_uiState.value.offsetY + pan.y) + (centroid.y - _uiState.value.focalY) * (1 - zoom),
//              rotationState += rotation
            )
        }
    }

    fun downloadAndSaveImage(
        url: String
    ) {
        // Chạy trong Coroutine
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _uiState.update { it.copy(isDownloading = true) }
                // Tải ảnh từ URL
                val connection = URL(url).openConnection()
                connection.connect()
                val inputStream = connection.getInputStream()
                val bitmap = BitmapFactory.decodeStream(inputStream)

                saveImageToStorage(bitmap, "downloaded_image_${System.currentTimeMillis()}")

                Log.wtf("download img", "ok")
                withContext(Dispatchers.Main) {
                    Toast.makeText(application, "Tải ảnh thành công.", Toast.LENGTH_SHORT).show()
                }

                _uiState.update { it.copy(isDownloading = false) }
                Log.wtf("aa", "ok")
            } catch (e: Exception) {
                Log.wtf("Image download", e.message)
                withContext(Dispatchers.Main) {
                    Toast.makeText(application, "Tải ảnh thất bại.", Toast.LENGTH_SHORT).show()
                }
                _uiState.update { it.copy(isDownloading = false) }
            }
        }
    }

    private fun saveImageToStorage(bitmap: Bitmap, fileName: String) {
        try {
            val directory = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)

            if (directory != null) {
                Log.wtf("Save diẻctory", directory.path)
            }

            if (directory != null) {
                val file = File(directory, "$fileName.jpg")
                val outputStream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.flush()
                outputStream.close()
                Log.wtf("Save imaghe", "saved")
            }
        } catch (e: Exception) {
            Log.wtf("Save imaghe", e.message)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as FacebookApplication)
                ImageViewModel(application)
            }
        }
    }
}

data class ImageUIState(
    val scale: Float = 1f,
    val rotationState: Float = 1f,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    val focalX: Float = 0f,
    val focalY: Float = 0f,
    val isDownloading: Boolean = false
)