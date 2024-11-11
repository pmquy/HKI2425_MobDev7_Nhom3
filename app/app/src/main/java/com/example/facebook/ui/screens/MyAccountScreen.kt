package com.example.facebook.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.facebook.ui.FacebookScreen
import com.example.facebook.ui.components.File
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyAccountScreen(
    userViewModel: UserViewModel = viewModel(factory = UserViewModel.Factory),
    navController: NavHostController
) {
    val coroutineScope = rememberCoroutineScope()
    val uiState by userViewModel.uiState.collectAsState()

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Account") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                )
            )
        }
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {

            Text("My Account")

            Text("Name: ${uiState.user?.firstName} ${uiState.user?.lastName}")

            Text("Email: ${uiState.user?.email}")

            Text("Phone Number: ${uiState.user?.phoneNumber}")

            if(uiState.user?.avatar != null) {
                File(
                    uiState.user?.avatar!!, modifier = Modifier
                        .size(200.dp)
                        .clip(CircleShape)
                )
            }

            OutlinedButton(onClick = handleLogout) {
                Text("Log out")
            }
        }
    }
}