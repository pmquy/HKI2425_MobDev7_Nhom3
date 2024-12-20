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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

data class RegisterUiState(
    val email: String = "",
    val phoneNumber: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val showOtpPage: Boolean = false,
    val otp: String = "",
    val avatar: Pair<File, String>? = null,
    val currentStep: Int = 1
)

class RegisterViewModel(
    private val userRepository: UserRepository,
    private val application: FacebookApplication,
) : ViewModel() {
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState = _uiState.asStateFlow()

    val setEmail: (String) -> Unit = { _uiState.value = _uiState.value.copy(email = it) }
    val setPassword: (String) -> Unit = { _uiState.value = _uiState.value.copy(password = it) }
    val setConfirmPassword: (String) -> Unit =
        { _uiState.value = _uiState.value.copy(confirmPassword = it) }
    val setFirstName: (String) -> Unit =
        { _uiState.value = _uiState.value.copy(firstName = it) }
    val setLastName: (String) -> Unit = { _uiState.value = _uiState.value.copy(lastName = it) }
    val setAvatar: (Pair<File, String>) -> Unit =
        { _uiState.value = _uiState.value.copy(avatar = it) }
    val setOtp: (String) -> Unit = { _uiState.value = _uiState.value.copy(otp = it) }
    val setPhoneNumber: (String) -> Unit =
        { _uiState.value = _uiState.value.copy(phoneNumber = it) }
    val setCurrentStep: (Int) -> Unit = { _uiState.value = _uiState.value.copy(currentStep = it) }
    suspend fun otp() {
        val response = userRepository.verifyOtp(uiState.value.email, uiState.value.otp)
        if (!response.isSuccessful) throw Exception("Error sending OTP")
    }

    val handleRegister: () -> Unit = {
        viewModelScope.launch {
            try {
                if (uiState.value.email.isEmpty() || uiState.value.password.isEmpty() || uiState.value.firstName.isEmpty() || uiState.value.lastName.isEmpty() || uiState.value.avatar == null) {
                    throw Exception("All fields are required")
                }
                if (uiState.value.password != uiState.value.confirmPassword) {
                    throw Exception("Passwords do not match")
                }
                val response = userRepository.register(
                    firstName = uiState.value.firstName,
                    lastName = uiState.value.lastName,
                    email = uiState.value.email,
                    password = uiState.value.password,
                    phoneNumber = uiState.value.phoneNumber,
                    avatar = uiState.value.avatar
                )
                if (!response.isSuccessful) throw Exception("Error registering")
                _uiState.value = _uiState.value.copy(showOtpPage = true)
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
                val socketRepository = application.container.socketRepository
                RegisterViewModel(
                    userRepository = userRepository,
                    application = application
                )
            }
        }
    }

}