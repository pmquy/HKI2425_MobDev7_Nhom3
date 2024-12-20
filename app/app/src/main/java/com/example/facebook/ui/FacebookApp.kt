package com.example.facebook.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.facebook.ui.screens.ChatGroupScreen
import com.example.facebook.ui.screens.CreateChatGroupScreen
import com.example.facebook.ui.screens.FindMessageScreen
import com.example.facebook.ui.screens.FindUserScreen
import com.example.facebook.ui.screens.FriendsScreen
import com.example.facebook.ui.screens.HomeScreen
import com.example.facebook.ui.screens.ImageViewScreen
import com.example.facebook.ui.screens.LoginScreen
import com.example.facebook.ui.screens.ProfileScreen
import com.example.facebook.ui.screens.SignUpScreen
import com.example.facebook.ui.screens.UserViewModel
import com.example.facebook.ui.screens.VideoCallScreen

enum class FacebookScreen {
    HOME,
    LOGIN,
    SIGNUP,
    CHAT_GROUP,
    LOADING,
    VIDEO_CALL,
    FRIENDS,
    FRIEND_SEARCHING,
    PROFILE,
    CREATE_CHAT_GROUP,
    FIND_MESSAGE,
    IMAGE_VIEW
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
                FacebookScreen.FRIENDS -> Text("Friends")
                FacebookScreen.FRIEND_SEARCHING -> Text("Search")
                FacebookScreen.PROFILE -> Text("Profile")
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

@Composable
fun FacebookApp(startDestination: String?) {
    val userViewModel: UserViewModel = viewModel(factory = UserViewModel.Factory)
    val context = LocalContext.current
    val navController = rememberNavController()

    NavHost(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        navController = navController,
        startDestination = FacebookScreen.LOADING.name
    ) {
        composable(FacebookScreen.LOADING.name) {
            val permissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) {}

            val locationPermissionRequest = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) {}

            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                locationPermissionRequest.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                    )
                )

                try {
                    userViewModel.auth()
                    navController.navigate(startDestination ?: FacebookScreen.HOME.name)
                } catch (e: Exception) {
                    navController.navigate(FacebookScreen.LOGIN.name)
                }
            }

            Text("Loading")
        }
        composable(FacebookScreen.HOME.name) {
            HomeScreen(navController = navController, userViewModel = userViewModel)
        }
        composable(FacebookScreen.SIGNUP.name) {
            SignUpScreen(navController = navController)
        }
        composable(FacebookScreen.LOGIN.name) {
            LoginScreen(navController = navController)
        }
        composable("${FacebookScreen.CHAT_GROUP.name}/{id}") {
            ChatGroupScreen(navController = navController)
        }
        composable("${FacebookScreen.VIDEO_CALL.name}/{id}") {
            VideoCallScreen(navController = navController)
        }
        composable(FacebookScreen.FRIENDS.name) {
            FriendsScreen(userViewModel, navController = navController)
        }
        composable(FacebookScreen.FRIEND_SEARCHING.name) {
            FindUserScreen(navController = navController)
        }
        composable(FacebookScreen.CREATE_CHAT_GROUP.name) {
            CreateChatGroupScreen(navController = navController)
        }
        composable("${FacebookScreen.PROFILE.name}/{id}") {
            ProfileScreen(navController = navController)
        }
        composable("${FacebookScreen.FIND_MESSAGE.name}/{id}") {
            FindMessageScreen(navController = navController)
        }
        composable("${FacebookScreen.IMAGE_VIEW.name}/{id}") {
            ImageViewScreen(navController = navController)
        }
    }
}
