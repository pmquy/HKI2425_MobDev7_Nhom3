package com.example.facebook.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.facebook.ui.FacebookScreen
import com.example.facebook.ui.components.File
import com.example.facebook.ui.components.ImagePicker
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateChatGroupScreen(
    navController: NavHostController,
    friendViewModel: FriendViewModel = viewModel(factory = FriendViewModel.Factory),
    createChatGroupViewModel: CreateChatGroupViewModel = viewModel(factory = CreateChatGroupViewModel.Factory),
    userViewModel: UserViewModel = viewModel(factory = UserViewModel.Factory),
) {
    val coroutineScope = rememberCoroutineScope()
    val uiState = createChatGroupViewModel.uiState.collectAsState().value
    val friends = friendViewModel.uiState.collectAsState().value.friends
    val context = LocalContext.current

    val handleCreateChatGroup: () -> Unit = {
        coroutineScope.launch {
            try {
                createChatGroupViewModel.createChatGroup()
                navController.navigate(FacebookScreen.HOME.name)
            } catch (e: Exception) {
                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
            }
        }

    }

    LaunchedEffect(Unit) {
        friendViewModel.getFriends()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create new group chat") },
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
        }
    ) { it ->
        Column(
            modifier = Modifier
                .padding(it)
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = uiState.name,
                onValueChange = { createChatGroupViewModel.setName(it) },
                label = { Text("Group name") }
            )
            if (uiState.avatar != null) {
                AsyncImage(
                    model = uiState.avatar,
                    contentDescription = "Group avatar",
                    modifier = Modifier
                        .size(200.dp)
                        .clip(shape = CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
            ImagePicker { createChatGroupViewModel.setAvatar(it) }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                friends.forEach { friend ->
                    val user = userViewModel.getUserById(friend.from).value
                    if (user != null) {
                        val enabled = !createChatGroupViewModel.checkMember(user._id)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            File(
                                id = user.avatar,
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                            )
                            Text(user.firstName + " " + user.lastName)
                            Spacer(modifier = Modifier.weight(1f))
                            Button(
                                onClick = { createChatGroupViewModel.addMember(user._id) },
                                enabled = enabled
                            ) {
                                Text(if (enabled) "Add" else "Added")
                            }
                        }
                    }
                }

            }

            Button(onClick = handleCreateChatGroup) {
                Text("Create")
            }
        }
    }
}