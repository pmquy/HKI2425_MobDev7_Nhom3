package com.example.facebook.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.facebook.model.User
import com.example.facebook.ui.components.File

@Composable
fun FriendsScreen(
    userViewModel: UserViewModel,
    friendsViewModel: FriendsViewModel = viewModel(factory = FriendsViewModel.Factory),
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    var uiState = friendsViewModel.uiState.collectAsState()

    LaunchedEffect(true) {
        try {
            friendsViewModel.getFriends()
            friendsViewModel.getRequests()
            friendsViewModel.getSuggestions()
            friendsViewModel.getSends()
            Log.e("FriendScreen", "GetAlls", )
        } catch (e: Exception) {
            Log.e("FriendScreen", "Error getting friends", e)
        }
    }

    Scaffold (
        topBar = { TopBar(navController) }
    ) { contentPadding ->
        Column (
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .padding(10.dp)
            ) {
                Card  (
                    onClick = {
                        friendsViewModel.changeSubScreen(FriendSubScreen.SUGGESTS)
                        Log.e("FriendScreeen", uiState.value.suggestions.toString())
                              },
                    content = {
                        Text("Gợi ý", modifier = Modifier.padding(10.dp))
                    }
                )
                Card (
                    onClick = {
                        friendsViewModel.changeSubScreen(FriendSubScreen.REQUESTS)
                              },
                    content = {
                        Text("Lời mời kết bạn", modifier = Modifier.padding(10.dp))
                    }
                )
                Card (
                    onClick = {
                        friendsViewModel.changeSubScreen(FriendSubScreen.ALL)
                        Log.e("FriendScreeen", uiState.value.friends.toString())
                              },
                    content = {
                        Text("Tất cả bạn bè", modifier = Modifier.padding(10.dp))
                    }
                )
            }

            when (uiState.value.currentSubScreen) {
                FriendSubScreen.SUGGESTS -> FriendSuggestion(
                    uiState.value.suggestions,
                    friendsViewModel,
                    userViewModel
                )
                FriendSubScreen.REQUESTS -> FriendRequestList(
                    uiState.value.requests.map {friend -> friend.from},
                    friendsViewModel,
                    userViewModel
                )
                FriendSubScreen.ALL -> AllFriendsList(
                    uiState.value.friends.map {friend -> friend.from},
                    friendsViewModel,
                    userViewModel
                )
            }
        }
    }
}

@Composable
fun FriendSuggestion(
    friends: List<String>,
    friendsViewModel: FriendsViewModel,
    userViewModel: UserViewModel,
    modifier: Modifier = Modifier
) {
    FriendList(
        friends,
        userViewModel,
        { friend ->
            Row {
                Button(
                    onClick = {
                        friendsViewModel.request(friend._id)
                    },
                ) {
                    Text("Thêm bạn bè")
                }
                Spacer(modifier = Modifier.width(10.dp))
                Button(
                    onClick = {friendsViewModel.decline(friend._id)}
                ) {
                    Text("Xoá")
                }
            }
        }
    )
}

@Composable
fun FriendRequestList(
    friends: List<String>,
    friendsViewModel: FriendsViewModel,
    userViewModel: UserViewModel,
    modifier: Modifier = Modifier)
{
    FriendList(
        friends,
        userViewModel,
        { friend->
            Row {
                Button(
                    onClick = {
                        friendsViewModel.accept(friend._id)
                    },
                ) {
                    Text("Châps nhận")
                }
                Spacer(modifier = Modifier.width(10.dp))
                Button(
                    onClick = {}
                ) {
                    Text("Xoá")
                }
            }
        }
    )
}


@Composable
fun AllFriendsList(
    friends: List<String>,
    friendsViewModel: FriendsViewModel,
    userViewModel: UserViewModel,
    modifier: Modifier = Modifier)
{
    Log.e("FriendScreen", "Call all friends")
    Log.e("FriendScreen", friends.toString())
    FriendList(
        friends,
        userViewModel,
        { friend ->
            Row {
                Button(
                    onClick = {friendsViewModel.disfriend(friend._id)}
                ) {
                    Text("Xoá bạn bè")
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(navController: NavHostController, modifier: Modifier = Modifier) {
    CenterAlignedTopAppBar(
        title = {
            Text("Friends")
        },
        navigationIcon = {
            IconButton(
                onClick = {
                    navController.navigateUp()
                }
            ) {
                Icon(Icons.Filled.ArrowBack, contentDescription = null)
            }
        }
    )
}

@Composable
fun FriendList(
    friends: List<String>,
    userViewModel: UserViewModel,
    friendCardButtons: @Composable RowScope.(User) -> Unit,
    modifier: Modifier = Modifier)
{
    LazyColumn() {
        items(friends) { friend ->
            val user : User? = userViewModel.getUserById(friend).collectAsState().value
            if (user != null) {
                FriendCard(user, friendCardButtons)
            }
        }
    }
}

@Composable
fun FriendCard(
    friend: User,
    otherContent: @Composable RowScope.(user: User) -> Unit,
    modifier: Modifier = Modifier)
{
    Card(
        modifier = Modifier
            .padding(20.dp)
            .fillMaxWidth()
    ) {
        Row (
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            File(
                id = friend.avatar,
                modifier = Modifier
                    .size(75.dp)
                    .clip(CircleShape)
            )
            Column(
                modifier = Modifier
                    .padding(10.dp)
            ) {
                Text(
                    text = friend.firstName + friend.lastName,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .padding(10.dp)
                )
                Row {
                    otherContent(friend)
                }
            }
        }
    }
}
