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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

class ProfileViewModel(
    private val userRepository: UserRepository,
    private val application: FacebookApplication,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUIState())
    val uiState = _uiState.asStateFlow()

    fun getUserById(id: String) {
        viewModelScope.launch {
            try {
                val user = userRepository.getById(id).body()
                _uiState.update { it.copy(user = user) }
            } catch (e: Exception) {
                Toast.makeText(application, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun handleUpdate(firstName: String? = null, lastName: String? = null, phoneNumber: String? = null, avatar: Pair<File, String>? = null, password: String? = null) {
        viewModelScope.launch {
            try {
                userRepository.update(firstName, lastName, password, phoneNumber, avatar)
                getUserById(application.user._id)
            } catch (e: Exception) {
                Toast.makeText(application, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as FacebookApplication)
                val userRepository = application.container.userRepository
                ProfileViewModel(
                    userRepository = userRepository,
                    application = application
                )
            }
        }
    }
}

data class ProfileUIState(
    val user: User? = null
)