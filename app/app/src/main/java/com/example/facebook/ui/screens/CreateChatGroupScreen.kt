package com.example.facebook.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.example.facebook.ui.FacebookScreen
import com.example.facebook.ui.components.File
import com.example.facebook.ui.components.ImagePicker
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateChatGroupScreen(
    navController: NavHostController,
    friendViewModel: FriendsViewModel = viewModel(factory = FriendsViewModel.Factory),
    createChatGroupViewModel: CreateChatGroupViewModel = viewModel(factory = CreateChatGroupViewModel.Factory),
    userViewModel: UserViewModel = viewModel(factory = UserViewModel.Factory),
) {
    val coroutineScope = rememberCoroutineScope()
    val uiState by createChatGroupViewModel.uiState.collectAsStateWithLifecycle()
    val friendState by friendViewModel.uiState.collectAsStateWithLifecycle()
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
                label = { Text("Group name") },
                modifier = Modifier.fillMaxWidth()
            )
            if (uiState.avatar != null) {
                AsyncImage(
                    model = uiState.avatar!!.first,
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

                friendState.friends.forEach { friend ->
                    val user = userViewModel.getUserById(friend.from).value
                    if (user != null) {
                        val added = createChatGroupViewModel.checkMember(user._id)
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
                                {
                                    if (added) {
                                        createChatGroupViewModel.removeMember(user._id)
                                    } else {
                                        createChatGroupViewModel.addMember(user._id)
                                    }
                                }
                            ) {
                                Text(if (added) "Remove" else "Add")
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