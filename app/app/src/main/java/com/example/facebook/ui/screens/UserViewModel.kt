package com.example.facebook.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.facebook.FacebookApplication
import com.example.facebook.data.SocketRepository
import com.example.facebook.data.UserRepository
import com.example.facebook.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UIState(
    var user: User? = null
)

class UserViewModel(
    private val userRepository: UserRepository,
    private val socketRepository: SocketRepository,
    private val application: FacebookApplication
) : ViewModel() {

    private val _uiState = MutableStateFlow(UIState())
    val uiState = _uiState.asStateFlow()


    suspend fun login(email: String, password: String) {
        val response = userRepository.login(email, password)
        if (!response.isSuccessful) throw Exception("Error logging in")
        application.user = response.body()!!
    }

    suspend fun auth(token: String): Boolean {
        val response = userRepository.auth(token, socketRepository.getID())
        if (!response.isSuccessful) return false
        application.user = response.body()!!
        return true
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as FacebookApplication)
                val userRepository = application.container.userRepository
                val socketRepository = application.container.socketRepository
                UserViewModel(
                    userRepository = userRepository,
                    socketRepository = socketRepository,
                    application = application
                )
            }
        }
    }

}