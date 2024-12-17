package com.example.facebook.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.facebook.FacebookApplication
import com.example.facebook.data.ChatGroupRepository
import com.example.facebook.model.ChatGroup
import com.example.facebook.model.Message
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class FindMessageViewModel(
    private val chatGroupRepository: ChatGroupRepository,
    private val application: FacebookApplication
) : ViewModel() {
    private val _uiState = MutableStateFlow(FindMessageUIState())
    val uiState = _uiState.asStateFlow()
    var searchJob: Job? = null

    private val LIMIT = 100

    val setSearch = { search: String ->
        _uiState.update {
            searchJob?.cancel()
            searchJob = findMessages()
            it.copy(search = search, offset = 0, messages = listOf())
        }
    }

    fun findMessages(): Job {
        return viewModelScope.launch {
            delay(500)
            try {
                val response = chatGroupRepository.getMessage(
                    id = _uiState.value.chatGroupId,
                    offset = _uiState.value.offset,
                    limit = LIMIT,
                    query = Json.encodeToString(mapOf("message" to _uiState.value.search))
                )
                Log.wtf("find message", response.toString())
                if (!response.isSuccessful) throw Exception("Error retrieving messages")
                _uiState.update {
                    it.copy(
                        messages = response.body()!!.data,
                        hasMore = response.body()!!.hasMore,
                        offset = it.offset + LIMIT,
                        error = null  // Clear error if successful
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message)  // Update UIState with the error message instead of displaying a Toast
                }
            }
        }
    }

    fun setChatGroupId(id: String) {
        _uiState.update {
            it.copy(chatGroupId = id)
        }
    }

    fun clearInput() {
        _uiState.update {
            searchJob?.cancel()
            it.copy(search = "", hasMore = false, offset = 0, messages = listOf())
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as FacebookApplication)
                val chatGroupRepository = application.container.chatGroupRepository

                FindMessageViewModel(
                    chatGroupRepository, application
                )
            }
        }
    }
}

data class FindMessageUIState(
    val chatGroupId: String = "",
    val search: String = "",
    val hasMore: Boolean = false,
    val offset: Int = 0,
    val messages: List<Message> = listOf(),
    val error: String? = null
)