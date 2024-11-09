package com.example.facebook.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.facebook.ui.screens.ChatGroupScreen
import com.example.facebook.ui.screens.HomeScreen
import com.example.facebook.ui.screens.LoginScreen
import com.example.facebook.ui.screens.UserViewModel
import com.example.facebook.ui.screens.VideoCallScreen
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch

enum class FacebookScreen {
    HOME,
    LOGIN,
    CHAT_GROUP,
    LOADING,
    VIDEO_CALL,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FacebookTopBar(
    currentScreen: FacebookScreen,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit
) {
    TopAppBar(
        colors = topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(),
        title = {
            when (currentScreen) {
                FacebookScreen.HOME -> Text("Home")
                FacebookScreen.LOGIN -> Text("Login")
                FacebookScreen.CHAT_GROUP -> Text("Chat Group")
                else -> Text("")
            }
        },
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Back"
                    )
                }
            }
        }
    )
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun FacebookApp() {
    val userViewModel: UserViewModel = viewModel(factory = UserViewModel.Factory)

    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(true) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            coroutineScope.launch {
                val success = userViewModel.auth(it.result)
                if (success) navController.navigate(FacebookScreen.HOME.name)
                else navController.navigate(FacebookScreen.LOGIN.name)
            }
        }
    }

    NavHost(
        modifier = Modifier.fillMaxSize(),
        navController = navController,
        startDestination = FacebookScreen.LOADING.name
    ) {
        composable(FacebookScreen.LOADING.name) {
            Text("Loading")
        }
        composable(FacebookScreen.HOME.name) {
            HomeScreen(navController = navController)
        }
        composable(FacebookScreen.LOGIN.name) {
            LoginScreen(navController =  navController)
        }
        composable("${FacebookScreen.CHAT_GROUP.name}/{id}") {
            ChatGroupScreen(navController =  navController)
        }
        composable("${FacebookScreen.VIDEO_CALL.name}/{id}") {
            VideoCallScreen(navController =  navController)
        }
    }

}