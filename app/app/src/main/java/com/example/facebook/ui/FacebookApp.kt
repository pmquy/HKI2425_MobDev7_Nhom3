package com.example.facebook.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.facebook.ui.screens.ChatGroupScreen
import com.example.facebook.ui.screens.CreateChatGroupScreen
import com.example.facebook.ui.screens.FindUserScreen
import com.example.facebook.ui.screens.FriendScreen
import com.example.facebook.ui.screens.HomeScreen
import com.example.facebook.ui.screens.LoginScreen
import com.example.facebook.ui.screens.MyAccountScreen
import com.example.facebook.ui.screens.ProfileScreen
import com.example.facebook.ui.screens.RegisterScreen
import com.example.facebook.ui.screens.UserViewModel
import com.example.facebook.ui.screens.VideoCallScreen

enum class FacebookScreen {
    HOME,
    LOGIN,
    CHAT_GROUP,
    LOADING,
    VIDEO_CALL,
    REGISTER,
    MY_ACCOUNT,
    FRIENDS,
    CREATE_CHAT_GROUP,
    FIND_USER,
    PROFILE,
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun FacebookApp() {
    val userViewModel: UserViewModel = viewModel(factory = UserViewModel.Factory)

    val navController = rememberNavController()

    LaunchedEffect(Unit) {
        try {
            userViewModel.auth()
            navController.navigate(FacebookScreen.HOME.name)
        } catch (e: Exception) {
            navController.navigate(FacebookScreen.LOGIN.name)
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
        composable(FacebookScreen.REGISTER.name) {
            RegisterScreen(navController =  navController)
        }
        composable(FacebookScreen.MY_ACCOUNT.name) {
            MyAccountScreen(navController =  navController)
        }
        composable(FacebookScreen.FRIENDS.name) {
            FriendScreen(navController =  navController)
        }
        composable(FacebookScreen.CREATE_CHAT_GROUP.name) {
            CreateChatGroupScreen(navController = navController)
        }

        composable(FacebookScreen.FIND_USER.name) {
            FindUserScreen(navController = navController)
        }

        composable("${FacebookScreen.PROFILE.name}/{id}") {
            ProfileScreen(navController = navController)
        }
    }

}