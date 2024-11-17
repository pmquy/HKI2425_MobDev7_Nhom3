package com.example.facebook.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.facebook.ui.FacebookScreen
import com.example.facebook.ui.components.ImagePicker
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(
    registerViewModel: RegisterViewModel = viewModel(factory = RegisterViewModel.Factory),
    navController: NavHostController
) {
    val coroutineScope = rememberCoroutineScope()
    val uiState by registerViewModel.uiState.collectAsState()
    val context = LocalContext.current

    val handleOtp: () -> Unit = {
        coroutineScope.launch {
            try {
                registerViewModel.otp()
                navController.navigate(FacebookScreen.LOGIN.name)
            } catch (e: Exception) {
                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }


    Scaffold {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
        ) {
            Column (
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if(uiState.showOtpPage) {
                    OutlinedTextField(
                        value = uiState.otp,
                        onValueChange = registerViewModel.setOtp,
                        label = { Text("OTP") },
                    )
                    Button(onClick = handleOtp) {
                        Text("Submit OTP")
                    }
                } else {

                    OutlinedTextField(
                        value = uiState.email,
                        onValueChange = registerViewModel.setEmail,
                        label = { Text("Email") },
                    )
                    OutlinedTextField(
                        value = uiState.phoneNumber,
                        onValueChange = registerViewModel.setPhoneNumber,
                        label = { Text("Phone Number") },
                    )
                    OutlinedTextField(
                        value = uiState.firstName,
                        onValueChange = registerViewModel.setFirstName,
                        label = { Text("First Name") },
                    )
                    OutlinedTextField(
                        value = uiState.lastName,
                        onValueChange = registerViewModel.setLastName,
                        label = { Text("Last Name") },
                    )
                    OutlinedTextField(
                        value = uiState.password,
                        onValueChange = registerViewModel.setPassword,
                        label = { Text("Password") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )
                    OutlinedTextField(
                        value = uiState.confirmPassword,
                        onValueChange = registerViewModel.setConfirmPassword,
                        label = { Text("Confirm Password") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )
                    if (uiState.avatar != null) {
                        AsyncImage(
                            model = uiState.avatar?.first,
                            contentDescription = "Avatar",
                            modifier = Modifier
                                .size(100.dp)
                                .clip(shape = CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                    ImagePicker {
                        registerViewModel.setAvatar(it)
                    }
                    Button(onClick = registerViewModel.handleRegister) {
                        Text("Register")
                    }
                }

            }
        }
    }
}