package com.example.facebook.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.facebook.model.User
import com.example.facebook.ui.FacebookScreen
import com.example.facebook.ui.components.File
import com.example.facebook.ui.components.ImagePicker
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyAccountScreen(
    userViewModel: UserViewModel = viewModel(factory = UserViewModel.Factory),
    navController: NavHostController
) {
    val coroutineScope = rememberCoroutineScope()
    val user = userViewModel.uiState.collectAsState().value.user

    val handleLogout: () -> Unit = {
        coroutineScope.launch {
            try {
                userViewModel.logout()
                navController.navigate(FacebookScreen.LOGIN.name)
            } catch (e: Exception) {
                Toast.makeText(navController.context, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(topBar = {
        TopAppBar(title = { Text("My Account") }, navigationIcon = {
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        }, colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        )
        )
    }) {

        if(user != null) {
            Column(
                modifier = Modifier
                    .padding(it)
                    .padding(8.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {


                Text("My Account")

                Text("Name: ${user.firstName} ${user.lastName}")

                Text("Email: ${user.email}")

                Text("Phone Number: ${user.phoneNumber}")

                if(user.avatar.isNotEmpty()) {
                    File(
                        user.avatar, modifier = Modifier
                            .size(200.dp)
                            .clip(CircleShape)
                    )
                }


                UpdateInfo(user) { firstName, lastName, phoneNumber ->
                    userViewModel.handleUpdate(
                        firstName = firstName, lastName = lastName, phoneNumber = phoneNumber
                    )
                }

                ChangePassword() {
                    userViewModel.handleUpdate(password = it)
                }

                UpdateAvatar() {
                    userViewModel.handleUpdate(avatar = it)
                }

                OutlinedButton(onClick = handleLogout) {
                    Text("Log out")
                }
            }
        }
    }
}

@Composable
fun UpdateInfo(
    user: User, onUpdate: (firstName: String?, lastName: String?, phoneNumber: String) -> Unit
) {

    var firstName by remember { mutableStateOf(user.firstName) }
    var lastName by remember { mutableStateOf(user.lastName) }
    val phoneNumber by remember { mutableStateOf(user.phoneNumber) }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(value = firstName,
            placeholder = { Text("First name") },
            onValueChange = { firstName = it })

        OutlinedTextField(value = lastName,
            placeholder = { Text("Last name") },
            onValueChange = { lastName = it })

        OutlinedTextField(value = phoneNumber,
            placeholder = { Text("Phone number") },
            onValueChange = {})

        Button(onClick = {
            onUpdate(firstName, lastName, phoneNumber)
        }) {
            Text("Update")
        }
    }

}

@Composable
fun ChangePassword(
    onUpdate: (password: String) -> Unit
) {

    val context = LocalContext.current
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedTextField(value = password,
            placeholder = { Text("Password") },
            onValueChange = { password = it })

        OutlinedTextField(value = confirmPassword,
            placeholder = { Text("Confirm password") },
            onValueChange = { confirmPassword = it })

        Button(onClick = {
            if (password != confirmPassword) {
                Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
            } else {
                onUpdate(password)
            }
        }) {
            Text("Update")
        }
    }
}

@Composable
fun UpdateAvatar(
    onUpdate: (avatar: Pair<File, String>) -> Unit
) {

    var avatar by remember { mutableStateOf<Pair<File, String>?>(null) }

    ImagePicker {
        avatar = it
    }

    if(avatar != null) {
        AsyncImage(
            model = avatar?.first,
            contentDescription = "Avatar",
            modifier = Modifier
                .size(100.dp)
                .clip(shape = CircleShape),
            contentScale = ContentScale.Crop
        )
    }

    Button(onClick = {
        if (avatar != null) {
            onUpdate(avatar!!)
        }
    }) {
        Text("Update")
    }
}