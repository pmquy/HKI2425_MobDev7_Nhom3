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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.toLowerCase
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.facebook.model.User
import com.example.facebook.ui.FacebookScreen
import com.example.facebook.ui.FacebookTopBar
import com.example.facebook.ui.components.File
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
    userViewModel: UserViewModel,
    friendsViewModel: FriendsViewModel = viewModel(factory = FriendsViewModel.Factory),
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    var isSearching by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val uiState = friendsViewModel.uiState.collectAsState()

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

    LaunchedEffect(isSearching) {
        try {
            focusRequester.requestFocus()
        } catch (e: Exception) {
            Log.e("FriendScreen", "Focus Error: ", e)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    if (!isSearching) Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Friends")
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton({ isSearching = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                    } else TextField(
                        value = uiState.value.search,
                        placeholder = { Text("Tìm kiếm") },
                        onValueChange = friendsViewModel.setSearch,
                        singleLine = true,
                        trailingIcon = {
                            if (uiState.value.search.isNotEmpty()) Icon(
                                Icons.Default.Clear,
                                contentDescription = null,
                                modifier = Modifier
                                    .clickable {
                                        friendsViewModel.clearInput()
                                    }
                            )
                        },
                        colors = TextFieldDefaults.textFieldColors(
                            focusedTextColor = MaterialTheme.colorScheme.onBackground,
                            cursorColor = MaterialTheme.colorScheme.primary,
                            errorCursorColor = MaterialTheme.colorScheme.error,
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp, bottom = 10.dp)
                            .focusRequester(focusRequester)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (isSearching) {
                                isSearching = false
                            } else {
                                navController.navigateUp()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { contentPadding ->
        Column(
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
                    .testTag("FriendSubScreen")
            ) {
                FriendSubScreen.entries.forEach {
                    Card(
                        onClick = {
                            friendsViewModel.changeSubScreen(it)
                        },
                        content = {
                            Text(
                                it.tag,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(10.dp)
                            )
                        },
                        modifier = Modifier
                            .background(color = MaterialTheme.colorScheme.surface)
                    )
                }
            }

            val filter: (User) -> Boolean = { user ->
                "${user.firstName} ${user.lastName}"
                    .toLowerCase(Locale.ROOT)
                    .startsWith(uiState.value.search.toLowerCase(Locale.ROOT))

                        || "${user.lastName} ${user.firstName}"
                    .toLowerCase(Locale.ROOT)
                    .startsWith(uiState.value.search.toLowerCase(Locale.ROOT))
            }

            when (uiState.value.currentSubScreen) {
                FriendSubScreen.SUGGESTS -> FriendSuggestion(
                    friends = uiState.value.suggestions,
                    friendsViewModel = friendsViewModel,
                    userViewModel = userViewModel,
                    navController = navController,
                    filter = filter
                )

                FriendSubScreen.REQUESTS -> FriendRequestList(
                    friends = uiState.value.requests.map { friend -> friend.from },
                    friendsViewModel = friendsViewModel,
                    userViewModel = userViewModel,
                    navController = navController,
                    filter = filter
                )

                FriendSubScreen.SENTS -> FriendSents(
                    friends = uiState.value.sends.map { friend -> friend.to },
                    friendsViewModel = friendsViewModel,
                    userViewModel = userViewModel,
                    navController = navController,
                    filter = filter
                )

                FriendSubScreen.ALL -> AllFriendsList(
                    friends = uiState.value.friends.map { friend -> friend.from },
                    friendsViewModel = friendsViewModel,
                    userViewModel = userViewModel,
                    navController = navController,
                    filter = filter
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
    filter: (User) -> Boolean = { true },
    modifier: Modifier = Modifier.testTag("Sents")
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
        },
        filter
    )
}

@Composable
fun FriendSuggestion(
    friends: List<String>,
    friendsViewModel: FriendsViewModel,
    userViewModel: UserViewModel,
    navController: NavHostController,
    filter: (User) -> Boolean = { true },
    modifier: Modifier = Modifier.testTag("Suggestions")
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
        },
        filter
    )
}

@Composable
fun FriendRequestList(
    friends: List<String>,
    friendsViewModel: FriendsViewModel,
    userViewModel: UserViewModel,
    navController: NavHostController,
    filter: (User) -> Boolean = { true },
    modifier: Modifier = Modifier.testTag("Requests")
) {
    FriendList(
        friends,
        userViewModel,
        navController,
        { friend ->
            Row {
                Button(
                    onClick = {
                        friendsViewModel.accept(friend._id)
                        friendsViewModel.getRequests()
                    },
                ) {
                    Text("Chấp nhận")
                }
                Spacer(modifier = Modifier.width(10.dp))
                Button(
                    onClick = {
                        friendsViewModel.decline(friend._id)
                        friendsViewModel.getRequests()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Text("Xoá")
                }
            }
        },
        filter
    )
}

@Composable
fun AllFriendsList(
    friends: List<String>,
    friendsViewModel: FriendsViewModel,
    userViewModel: UserViewModel,
    navController: NavHostController,
    filter: (User) -> Boolean = { true },
    modifier: Modifier = Modifier.testTag("AllFriends")
) {
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
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Xoá bạn bè")
                }
            }
        },
        filter
    )
}

@Composable
fun FriendList(
    friends: List<String>,
    userViewModel: UserViewModel,
    navController: NavHostController,
    friendCardButtons: @Composable RowScope.(User) -> Unit,
    filter: (User) -> Boolean = { true },
    modifier: Modifier = Modifier
) {
    LazyColumn() {
        items(friends) { friend ->
            val user: User? = userViewModel.getUserById(friend).collectAsState().value
            if (user != null && filter(user)) {
                FriendCard(
                    user,
                    navController,
                    friendCardButtons
                )
            }
        }
    }
}

@Composable
fun FriendCard(
    user: User,
    navController: NavHostController,
    otherContent: @Composable RowScope.(user: User) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                navController.navigate("${FacebookScreen.PROFILE.name}/${user._id}")
            }
    ) {
        File(
            id = user.avatar,
            modifier = Modifier
                .padding(start = 20.dp, top = 10.dp, bottom = 10.dp)
                .size(75.dp)
                .clip(CircleShape)
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .padding(10.dp)
        ) {
            Text(
                text = "${user.firstName} ${user.lastName}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
            )
            Row {
                otherContent(user)
            }
        }
    }
}
