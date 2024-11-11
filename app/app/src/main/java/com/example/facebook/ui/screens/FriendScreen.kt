package com.example.facebook.ui.screens

import android.util.Log
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.facebook.model.User
import com.example.facebook.ui.components.File


@Composable
private fun UserAccount(
    user: User, modifier: Modifier = Modifier, content: @Composable () -> Unit = {}
) {
    Card(modifier = modifier, onClick = { }) {
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            File(
                id = user.avatar,
                Modifier
                    .size(48.dp)
                    .clip(CircleShape)
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = user.firstName + " " + user.lastName,
                    style = MaterialTheme.typography.bodyLarge,
                )
                content()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendScreen(
    userViewModel: UserViewModel = viewModel(factory = UserViewModel.Factory),
    friendViewModel: FriendViewModel = viewModel(factory = FriendViewModel.Factory),
    navController: NavHostController,
) {
    val uiState = friendViewModel.uiState.collectAsState()

    LaunchedEffect(true) {
        try {
            friendViewModel.getFriends()
            friendViewModel.getRequests()
            friendViewModel.getSuggestions()
            friendViewModel.getSends()
        } catch (e: Exception) {
            Log.e("FriendScreen", "Error getting friends", e)
        }
    }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Friends") },
            navigationIcon = {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.primary,
            ),
        )
    }) {
        Column(
            modifier = Modifier
                .padding(it)
                .padding(8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Requests")
            uiState.value.requests.forEach { friend ->
                val user = userViewModel.getUserById(friend.from).value
                if (user != null) {
                    UserAccount(user) {
                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.End),
                        ) {
                            Button({ friendViewModel.accept(user._id) }) {
                                Text("Confirm")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedButton({ friendViewModel.decline(user._id) }) {
                                Text("Decline")
                            }
                        }
                    }
                }
            }
            Text("Sends")
            uiState.value.sends.forEach { friend ->
                val user = userViewModel.getUserById(friend.to).value
                if (user != null) {
                    UserAccount(user) {
                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Button({ friendViewModel.revoke(user._id) }, ) {
                                Text("Revoke")
                            }
                        }
                    }
                }
            }
            Text("Suggestions")
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                uiState.value.suggestions.forEach {
                    val user = userViewModel.getUserById(it).value
                    if (user != null) {
                        Card {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(8.dp)
                            ) {
                                File(
                                    id = user.avatar,
                                    Modifier
                                        .size(100.dp)
                                        .clip(CircleShape)
                                )
                                Text(
                                    user.firstName + " " + user.lastName,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button({ friendViewModel.request(user._id) }) {
                                        Text("Add")
                                    }
                                    OutlinedButton({}) {
                                        Text("Remove")
                                    }
                                }
                            }
                        }
                    }
                }
            }


            Text("Friends")

            uiState.value.friends.forEach {
                val user = userViewModel.getUserById(it.from).value
                if(user != null) {
                    UserAccount(user) {
                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Button({ friendViewModel.disfriend(user._id) }, ) {
                                Text("Disfriend")
                            }
                        }
                    }
                }
            }

        }
    }
}