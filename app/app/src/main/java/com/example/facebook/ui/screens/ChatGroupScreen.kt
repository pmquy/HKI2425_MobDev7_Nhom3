package com.example.facebook.ui.screens

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.facebook.R
import com.example.facebook.model.Message
import com.example.facebook.model.User
import com.example.facebook.ui.FacebookScreen
import com.example.facebook.ui.components.File
import com.example.facebook.ui.components.MediaPicker
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatGroupScreen(
    chatGroupViewModel: ChatGroupViewModel = viewModel(factory = ChatGroupViewModel.Factory),
    navController: NavHostController
) {
    val uiState by chatGroupViewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    val id = navController.currentBackStackEntry?.arguments?.getString("id") ?: ""

    LaunchedEffect(Unit) {
        chatGroupViewModel.initChatGroup(id)
    }

    LaunchedEffect(scrollState.value) {
        if (scrollState.value == 0) {
            chatGroupViewModel.getMoreMessages()
        }
    }

    LaunchedEffect(uiState.newMessage, uiState.page) {
        if (uiState.newMessage > 0 || uiState.page == 1) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        File(
                            uiState.chatGroup.avatar, modifier = Modifier
                                .size(32.dp)
                                .clip(
                                    CircleShape
                                )
                        )
                        Text(uiState.chatGroup.name)
                    }
                },
                scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Call, contentDescription = "Call")
                    }
                    IconButton(onClick = { navController.navigate("${FacebookScreen.VIDEO_CALL.name}/$id") }) {
                        Icon(
                            painterResource(R.drawable.video_call),
                            contentDescription = "Video Call"
                        )
                    }
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Info, contentDescription = "Info")
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                CreateMessage { message, files ->
                    chatGroupViewModel.createMessage(message, files)
                }
            }
        }
    ) {
        Column {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
                    .verticalScroll(scrollState)
                    .weight(1f)
            ) {
                uiState.messages.forEach { message ->
                    key(message._id) {
                        MessageWrapper(
                            message = message,
                            chatGroupViewModel,
                            isMine = chatGroupViewModel.isMine(message)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MessageWrapper(
    message: Message,
    chatGroupViewModel: ChatGroupViewModel,
    isMine: Boolean,
    modifier: Modifier = Modifier
) {

    var user by remember { mutableStateOf(User(firstName = "Unknown")) }

    LaunchedEffect(message.user) {
        val response = chatGroupViewModel.getUserById(message.user)
        if (response.isSuccessful) {
            user = response.body()!!
        }
    }

    Row(
        modifier = modifier
            .padding(8.dp)
            .fillMaxWidth(),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom,
    ) {
        if (isMine) {
            Column(
                horizontalAlignment = Alignment.End
            ) {
                MessageMain(user = user, message = message)
            }
            Spacer(modifier = Modifier.width(8.dp))
            MessageUser(user)
        } else {
            MessageUser(user)
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                MessageMain(user = user, message = message)
            }
        }
    }
}

@Composable
fun MessageUser(user: User) {
    File(
        user.avatar,
        Modifier
            .size(32.dp)
            .clip(shape = CircleShape)
    )
}

@Composable
fun MessageMain(user: User, message: Message) {
    Text(user.firstName + " " + user.lastName)
    Card {
        message.files.forEach { file ->
            File(
                file,
                modifier = Modifier.sizeIn(maxWidth = 250.dp)
            )
        }
        if (message.message.isNotEmpty()) Text(message.message, modifier = Modifier.padding(8.dp))
    }
}

fun uriToFile(context: Context, uri: Uri): File {
    val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
    val fileName = "file.jpg"
    val file = File(context.cacheDir, fileName)
    val outputStream = FileOutputStream(file)
    inputStream?.use { input ->
        outputStream.use { output ->
            input.copyTo(output)
        }
    }
    Log.d("TEST", file.absolutePath)
    return file
}

@Composable
fun CreateMessage(modifier: Modifier = Modifier, onCreate: (message: String, files: List<File>) -> Unit) {
    var messageText by remember { mutableStateOf("") }
    var files by remember { mutableStateOf<List<File>>(emptyList()) }
    val context = LocalContext.current

    Column {

        Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextField(
                value = messageText,
                onValueChange = { messageText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a message") }
            )
            MediaPicker {
                files = it.map {
                    uriToFile(context, it)
                }
            }
            Button(onClick = {
                onCreate(messageText, files)
            }) {
                Text("Send")
            }
        }

    }

}