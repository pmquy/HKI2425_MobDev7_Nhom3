package com.example.facebook.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.facebook.model.ChatGroup
import com.example.facebook.ui.FacebookScreen
import com.example.facebook.ui.components.File
import com.example.facebook.util.parseDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory),
    userViewModel: UserViewModel,
    navController: NavHostController
) {
    val uiState by homeViewModel.uiState.collectAsState()

    LaunchedEffect(true) {
        try {
            homeViewModel.getAll()
        } catch (e: Exception) {
            Log.e("HomeScreen", "Error getting chat groups", e)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Facebook")
                        IconButton({navController.navigate(FacebookScreen.FRIEND_SEARCHING.name)}) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                        Spacer(modifier = Modifier.width(24.dp))
                        IconButton({}) {
                            Icon(Icons.Default.Home, contentDescription = "Home")
                        }
                        IconButton({navController.navigate(FacebookScreen.FRIENDS.name)}) {
                            Icon(Icons.Default.Person, contentDescription = "Friends")
                        }
                        IconButton({navController.navigate("${FacebookScreen.PROFILE.name}/${userViewModel.uiState.value.user._id}")}) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                        IconButton({}) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }

                    }
                },
                scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(FacebookScreen.CREATE_CHAT_GROUP.name) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Chat Group")
            }
        }
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .padding(it)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            uiState.chatGroups.forEach { chatGroup ->
                key(chatGroup._id) {
                    ChatGroup(
                        chatGroup = chatGroup,
                        onClick = { navController.navigate("${FacebookScreen.CHAT_GROUP.name}/$it") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )
                }
            }
        }
    }


}

@Composable
fun ChatGroup(chatGroup: ChatGroup, onClick: (String) -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier, onClick = { onClick(chatGroup._id) }, colors = CardColors(
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            disabledContentColor = MaterialTheme.colorScheme.onSurface,
            disabledContainerColor = MaterialTheme.colorScheme.surface,
        )
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            File(
                id = chatGroup.avatar,
                Modifier
                    .size(60.dp)
                    .clip(CircleShape),
                allowOrigin = false,
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = chatGroup.name)
                if (chatGroup.lastMessage != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = chatGroup.lastMessage.name + ": " + chatGroup.lastMessage.content,
                            modifier = Modifier.weight(1f),
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1
                        )
                        Text(parseDate(chatGroup.lastMessage.createdAt, "HH:mm"))
                    }
                }
            }
        }
    }
}