package com.example.facebook.ui.screens

import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.facebook.FacebookApplication
import com.example.facebook.data.UserRepository
import com.example.facebook.model.User
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class FindUserViewModel(
    private val application: FacebookApplication,
    private val userRepository: UserRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(FindUserUIState())
    val uiState = _uiState.asStateFlow()
    var searchJob: Job? = null

    private val LIMIT = 100

    val setSearch = { search: String ->
        _uiState.update {
            searchJob?.cancel()
            searchJob = findUsers()
            it.copy(search = search, offset = 0, users = listOf())
        }
    }

    fun findUsers(): Job {
        return viewModelScope.launch {
            delay(500)
            try {
                val response = userRepository.getUsers(
                    _uiState.value.offset,
                    LIMIT,
                    Json.encodeToString(mapOf("name" to _uiState.value.search))
                )
                if (!response.isSuccessful) throw Exception(response.message())
                _uiState.update {
                    it.copy(
                        users = it.users + response.body()!!.data,
                        hasMore = response.body()!!.hasMore,
                        offset = it.offset + LIMIT
                    )
                }
            } catch (e: Exception) {
                Toast.makeText(application, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun clearInput() {
        _uiState.update {
            searchJob?.cancel()
            it.copy(search = "", offset = 0, users = listOf())
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as FacebookApplication)
                val userRepository = application.container.userRepository
                FindUserViewModel(
                    userRepository = userRepository,
                    application = application
                )
            }
        }
    }
}

data class FindUserUIState(
    val users: List<User> = listOf(),
    val hasMore: Boolean = false,
    val offset: Int = 0,
    val search: String = "",
)