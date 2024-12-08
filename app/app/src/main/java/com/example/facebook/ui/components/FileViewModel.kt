package com.example.facebook.ui.components

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.facebook.FacebookApplication
import com.example.facebook.data.FileRepository
import com.example.facebook.data.SocketRepository
import com.example.facebook.model.File
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject

class FileViewModel(
    private val fileRepository: FileRepository,
    private val socketRepository: SocketRepository
) : ViewModel() {

    private val files = mutableMapOf<String, MutableStateFlow<File?>>()

    fun getFileById(id: String): MutableStateFlow<File?> {
        return files.getOrPut(id) {
            MutableStateFlow<File?>(null).also { flow ->
                viewModelScope.launch {
                    val response = fileRepository.getById(id)
                    if (response.isSuccessful) {
                        flow.value = response.body()
                    }
                }
            }
        }
    }

    suspend fun getSystemFile(type: String, offset: Int?, limit: Int?): List<File> {
        val response = fileRepository.getSystemFile(type, offset, limit)
        if (response.isSuccessful) {
            response.body()?.data?.forEach { file ->
                files[file._id] = MutableStateFlow(file)
            }
        }
        return response.body()?.data ?: listOf()
    }

    init {
        socketRepository.addEventListener("file_update") { it ->
            val id = it[0].toString()
            val update = it[1] as JSONObject
            Log.d("FileViewModel", "Received update for file $id: $update")
            files[id]?.update {
                it?.copy(
                    url = update.optString("url", it.url),
                    status = update.optString("status", it.status),
                    description = update.optString("description", it.description),
                    blurUrl = update.optString("blurUrl", it.blurUrl),
                )
            }
        }
    }


    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as FacebookApplication)
                val fileRepository = application.container.fileRepository
                val socketRepository = application.container.socketRepository
                FileViewModel(
                    fileRepository = fileRepository,
                    socketRepository = socketRepository
                )
            }
        }
    }

}