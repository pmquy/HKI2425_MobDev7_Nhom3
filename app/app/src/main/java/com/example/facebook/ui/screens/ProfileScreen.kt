package com.example.facebook.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.facebook.ui.components.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userViewModel: UserViewModel = viewModel(factory = UserViewModel.Factory),
    friendViewModel: FriendViewModel = viewModel(factory = FriendViewModel.Factory),
    navController : NavHostController,
) {
    val id = navController.currentBackStackEntry?.arguments?.getString("id") ?: ""
    val user = userViewModel.getUserById(id).collectAsState().value

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${user?.firstName} ${user?.lastName}") },
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
                .padding(8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if(user?.avatar != null) {
                File(
                    user.avatar, modifier = Modifier
                        .size(200.dp)
                        .clip(CircleShape)
                )
            }
            Text("Name: ${user?.firstName} ${user?.lastName}")
            Text("Email: ${user?.email}")
            Text("Phone: ${user?.phoneNumber}")


            when(user?.friendStatus) {
                "friend" -> {
                    Text("You and ${user.firstName} are friends")
                    Button(onClick = {
                        friendViewModel.disfriend(user._id)
                    }) {
                        Text("Disfriend")
                    }
                }
                "send" -> {
                    Text("You sent a friend request to ${user.firstName}")
                    Button( onClick = {
                        friendViewModel.revoke(user._id)
                    }) {
                        Text("Revoke")

                    }
                }
                "request" -> {
                    Text("${user.firstName} sent you a friend request")
                    Button(onClick = {
                        friendViewModel.accept(user._id)
                    }) {
                        Text("Accept")
                    }
                    Button(onClick = {
                        friendViewModel.decline(user._id)
                    }) {
                        Text("Decline")
                    }
                }
                "suggest" -> {
                    Text("You are not friends with ${user.firstName}")
                    Button(onClick = {
                        friendViewModel.request(user._id)
                    }) {
                        Text("Add friend")
                    }
                }
            }
        }
    }

}