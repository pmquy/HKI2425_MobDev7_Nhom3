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
import com.example.facebook.data.SocketRepository
import com.example.facebook.data.UserPreferenceRepository
import com.example.facebook.data.UserRepository
import com.example.facebook.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

data class UIState(
    var user: User,
)

class UserViewModel(
    private val userRepository: UserRepository,
    private val socketRepository: SocketRepository,
    private val userPreferenceRepository: UserPreferenceRepository,
    val application: FacebookApplication,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UIState(application.user))
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.asStateFlow().collect { state ->
                application.user = state.user
            }
        }
    }

    fun checkIfUser(id: String): Boolean {
        return application.user._id == id
    }

    private val users = mutableMapOf<String, MutableStateFlow<User?>>()

    fun getUserById(id: String): MutableStateFlow<User?> {
        return users.getOrPut(id) {
            MutableStateFlow<User?>(null).also { flow ->
                viewModelScope.launch {
                    val response = userRepository.getById(id)
                    if (response.isSuccessful) {
                        flow.value = response.body()
                    }
                }
            }
        }
    }

    suspend fun login(email: String, password: String) {
        val token = userPreferenceRepository.getToken()
        val socketId = socketRepository.getID()
        val response = userRepository.login(email, password, token, socketId)
        if (!response.isSuccessful) throw Exception("Error logging in")
        application.user = response.body()!!
        _uiState.update {
            it.copy(
                user = response.body()!!
            )
        }
        application.container.appCookieJar.loadCookies()
    }

    suspend fun auth() {
        val token = userPreferenceRepository.getToken()
        val socketId = socketRepository.getID()
        val response = userRepository.auth(token = token ?: "", socketId = socketId)
        if (!response.isSuccessful) throw Exception("Error authenticating")
        Log.d("user", response.body()!!._id)
        _uiState.update {
            it.copy(
                user = response.body()!!
            )
        }
    }

    fun handleUpdate(
        firstName: String? = null,
        lastName: String? = null,
        password: String? = null,
        phoneNumber: String? = null,
        avatar: Pair<File, String>? = null,
    ) {
        viewModelScope.launch {
            try {
                _uiState.update {
                    val response = userRepository.update(
                        firstName = firstName,
                        lastName = lastName,
                        password = password,
                        phoneNumber = phoneNumber,
                        avatar = avatar,
                    )
                    if (!response.isSuccessful) throw Exception("Error updating")
                    it.copy(
                        user = response.body()!!
                    )
                }
            } catch (e: Exception) {
                Toast.makeText(application, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    suspend fun logout() {
        val response = userRepository.logout()
        if (!response.isSuccessful) throw Exception("Error logging out")
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as FacebookApplication)
                val userRepository = application.container.userRepository
                val socketRepository = application.container.socketRepository
                val userPreferenceRepository = application.container.userPreferenceRepository
                UserViewModel(
                    userRepository = userRepository,
                    socketRepository = socketRepository,
                    userPreferenceRepository = userPreferenceRepository,
                    application = application
                )
            }
        }
    }

}