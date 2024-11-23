package com.example.facebook.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.example.facebook.ui.FacebookScreen
import com.example.facebook.ui.FacebookTopBar
import com.example.facebook.ui.components.File

@Composable
fun FriendsScreen(
    userViewModel: UserViewModel,
    friendsViewModel: FriendsViewModel = viewModel(factory = FriendsViewModel.Factory),
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    var uiState = friendsViewModel.uiState.collectAsState()

    LaunchedEffect(uiState.value.currentSubScreen) {
        try {
            friendsViewModel.getFriends()
            friendsViewModel.getRequests()
            friendsViewModel.getSuggestions()
            friendsViewModel.getSends()
        } catch (e: Exception) {
            Log.e("FriendScreen", "Error getting friends", e)
        }
    }

    Scaffold (
        topBar = {
            FacebookTopBar(
                FacebookScreen.FRIENDS,
                true
            ) {
                navController.navigateUp()
            }
        }
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
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                FriendSubScreen.entries.forEach {
                    Card  (
                        onClick = {
                            friendsViewModel.changeSubScreen(it)
                        },
                        content = {
                            Text(it.tag,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(10.dp))
                        },
                        modifier = Modifier
                            .background(color = MaterialTheme.colorScheme.surface)
                    )
                }
            }

            when (uiState.value.currentSubScreen) {
                FriendSubScreen.SUGGESTS -> FriendSuggestion(
                    uiState.value.suggestions,
                    friendsViewModel,
                    userViewModel,
                    navController
                )
                FriendSubScreen.REQUESTS -> FriendRequestList(
                    uiState.value.requests.map {friend -> friend.from},
                    friendsViewModel,
                    userViewModel,
                    navController
                )
                FriendSubScreen.SENTS -> FriendSents(
                    uiState.value.sends.map {friend -> friend.to},
                    friendsViewModel,
                    userViewModel,
                    navController
                )
                FriendSubScreen.ALL -> AllFriendsList(
                    uiState.value.friends.map {friend -> friend.from},
                    friendsViewModel,
                    userViewModel,
                    navController
                )
            }
        }
    }
}

@Composable
fun FriendSents(
    friends: List<String>,
    friendsViewModel: FriendsViewModel,
    userViewModel: UserViewModel,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    FriendList(
        friends,
        userViewModel,
        navController,
        { user ->
            Row {
                Button(
                    onClick = {
                        friendsViewModel.revoke(user._id)
                        friendsViewModel.getSends()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Huỷ")
                }
            }
        }
    )
}

@Composable
fun FriendSuggestion(
    friends: List<String>,
    friendsViewModel: FriendsViewModel,
    userViewModel: UserViewModel,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    FriendList(
        friends,
        userViewModel,
        navController,
        { user ->
            Row {
                Button(
                    onClick = {
                        friendsViewModel.request(user._id)
                        friendsViewModel.getSuggestions()
                    },
                    modifier = Modifier.weight(2f)
                ) {
                    Text("Thêm bạn bè")
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
    navController: NavHostController,
    modifier: Modifier = Modifier)
{
    FriendList(
        friends,
        userViewModel,
        navController,
        { friend->
            Row {
                Button(
                    onClick = {
                        friendsViewModel.accept(friend._id)
                        friendsViewModel.getRequests()
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
    navController: NavHostController,
    modifier: Modifier = Modifier)
{
    FriendList(
        friends,
        userViewModel,
        navController,
        { friend ->
            Row {
                Button(
                    onClick = {
                        friendsViewModel.disfriend(friend._id)
                        friendsViewModel.getFriends()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Text("Xoá bạn bè")
                }
            }
        }
    )
}

@Composable
fun FriendList(
    friends: List<String>,
    userViewModel: UserViewModel,
    navController: NavHostController,
    friendCardButtons: @Composable RowScope.(User) -> Unit,
    modifier: Modifier = Modifier)
{
    LazyColumn() {
        items(friends) { friend ->
            val user : User? = userViewModel.getUserById(friend).collectAsState().value
            if (user != null) {
                FriendCard(
                    user,
                    navController,
                    friendCardButtons)
            }
        }
    }
}

@Composable
fun FriendCard(
    user: User,
    navController: NavHostController,
    otherContent: @Composable RowScope.(user: User) -> Unit,
    modifier: Modifier = Modifier)
{
    Row (
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        File(
            id = user.avatar,
            modifier = Modifier
                .padding(start = 20.dp, top = 10.dp, bottom = 10.dp)
                .size(75.dp)
                .clip(CircleShape)
        )
        Column(
            modifier = Modifier
                .padding(10.dp)
        ) {
            Text(
                text = user.firstName + user.lastName,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .padding(10.dp)
            )
            Row {
                otherContent(user)
            }
        }
    }
}
