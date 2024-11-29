package com.example.facebook.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.example.facebook.R
import com.example.facebook.model.Message
import com.example.facebook.model.User
import com.example.facebook.ui.FacebookScreen
import com.example.facebook.ui.components.EmojiPicker
import com.example.facebook.ui.components.File
import com.example.facebook.ui.components.GifPicker
import com.example.facebook.ui.components.MediaPicker
import com.example.facebook.ui.components.MultipleImagePicker
import com.example.facebook.ui.components.VoiceRecorder
import com.example.facebook.util.createImageFile


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatGroupScreen(
    chatGroupViewModel: ChatGroupViewModel = viewModel(factory = ChatGroupViewModel.Factory),
    userViewModel: UserViewModel = viewModel(factory = UserViewModel.Factory),
    navController: NavHostController,
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

    LaunchedEffect(scrollState.maxValue) {
        if (scrollState.value >= scrollState.maxValue - 1000) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        File(
                            uiState.chatGroup.avatar, modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
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
                    IconButton(onClick = { navController.navigate("${FacebookScreen.VIDEO_CALL.name}/${id}") }) {
                        Icon(
                            painterResource(R.drawable.video_call),
                            contentDescription = "Video Call"
                        )
                    }
                    IconButton(onClick = {navController.navigate("${FacebookScreen.FIND_MESSAGE.name}/$id")}) {
                        Icon(Icons.Default.Info, contentDescription = "Info")
                    }
                }
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier.windowInsetsPadding(
                    WindowInsets.systemBars.only(
                        WindowInsetsSides.Bottom
                    )
                )
            ) {
                CreateMessage(
                    chatGroupViewModel = chatGroupViewModel,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .verticalScroll(scrollState)
        ) {
            uiState.messages.forEach { message ->
                key(message._id) {
                    val user = userViewModel.getUserById(message.user).collectAsState().value
                    if (user != null)
                        MessageWrapper(
                            message = message,
                            user = user,
                            isMine = chatGroupViewModel.isMine(message),
                        ) {
                            navController.navigate("${FacebookScreen.PROFILE.name}/${user._id}")
                        }
                }
            }
        }
    }
}

@Composable
fun MessageWrapper(
    message: Message,
    user: User,
    isMine: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {

    Row(
        modifier = modifier
            .padding(8.dp)
            .fillMaxWidth(),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom,
    ) {
        if (isMine) {
            MessageMain(user = user, message = message, isMine = true)
            Spacer(modifier = Modifier.width(8.dp))
            MessageUser(user, onClick = onClick)
        } else {
            MessageUser(user, onClick = onClick)
            Spacer(modifier = Modifier.width(8.dp))
            MessageMain(user = user, message = message, isMine = false)
        }
    }
}

@Composable
fun MessageUser(user: User, onClick: () -> Unit = {}) {
    File(
        user.avatar,
        Modifier
            .size(32.dp)
            .clip(shape = CircleShape)
            .clickable { onClick() },
        allowOrigin = false
    )
}

@Composable
fun MessageMain(user: User, message: Message, isMine: Boolean) {
    Column(
        horizontalAlignment = if (isMine) Alignment.End else Alignment.Start,
    ) {
        Text(user.firstName + " " + user.lastName)
        Card(
            modifier = Modifier.widthIn(max = 200.dp)
        ) {
            message.files.forEach { file ->
                File(id = file)
            }
            if (message.message.isNotEmpty()) Text(
                message.message,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateMessage(
    modifier: Modifier = Modifier,
    chatGroupViewModel: ChatGroupViewModel,
) {
    val uiState by chatGroupViewModel.uiState.collectAsState()
    val messageText = uiState.messageText
    val systemFiles = uiState.systemFiles
    val files = uiState.files
    val inputType = uiState.inputType
    val context = LocalContext.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {

        if (inputType == InputType.DEFAULT) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    modifier = Modifier.clickable {
                        chatGroupViewModel.setInputType(InputType.FILE)
                    },
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = "Send",
                )
                Icon(
                    painterResource(R.drawable.baseline_camera_alt_24),
                    modifier = Modifier.clickable {
                        chatGroupViewModel.setInputType(InputType.TAKE_PICTURE)
                    },
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = "Send",
                )
                Icon(
                    painterResource(R.drawable.baseline_image_24),
                    modifier = Modifier.clickable {
                        chatGroupViewModel.setInputType(InputType.IMAGE)
                    },
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = "Send",
                )
                Icon(
                    painterResource(R.drawable.baseline_mic_24),
                    modifier = Modifier.clickable {
                        chatGroupViewModel.setInputType(InputType.MIC)
                    },
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = "Send",
                )
            }
        } else {
            IconButton({ chatGroupViewModel.setInputType(InputType.DEFAULT) }) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    modifier = Modifier.clickable { chatGroupViewModel.setInputType(InputType.DEFAULT) },
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = "Send",
                )
            }
        }

        Row(
            modifier = Modifier
                .weight(1f)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    MaterialTheme.shapes.extraLarge
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BasicTextField(
                value = messageText,

                onValueChange = {
                    chatGroupViewModel.setMessageText(it)
                    chatGroupViewModel.setInputType(if (it.isEmpty()) InputType.DEFAULT else InputType.NORMAL)
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.shapes.extraLarge
                    ),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                decorationBox = { innerTextField ->
                    TextFieldDefaults.DecorationBox(
                        innerTextField = innerTextField,
                        enabled = true,
                        singleLine = true,
                        visualTransformation = androidx.compose.ui.text.input.VisualTransformation.None,
                        interactionSource = remember { MutableInteractionSource() },
                        contentPadding = PaddingValues(8.dp),
                        value = messageText,
                        shape = MaterialTheme.shapes.extraLarge,
                        colors = TextFieldDefaults.colors().copy(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                        )
                    )
                }
            )

            Icon(
                painterResource(R.drawable.baseline_insert_emoticon_24),
                modifier = Modifier
                    .padding(end = 4.dp)
                    .clickable { chatGroupViewModel.setInputType(InputType.EMOTICON) },
                tint = MaterialTheme.colorScheme.primary,
                contentDescription = "Send",
            )
        }

        IconButton(
            { chatGroupViewModel.createMessage(messageText, files, systemFiles) },
            enabled = messageText.isNotEmpty() || files.isNotEmpty() || systemFiles.isNotEmpty(),
        ) {
            BadgedBox(
                badge = {
                    if (files.isNotEmpty()) {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        ) {
                            Text("${files.size}")
                        }
                    }
                },
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send, contentDescription = "Send",
                )
            }
        }

    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        when (inputType) {

            InputType.EMOTICON -> {
                EmojiPicker(Modifier.heightIn(max = 300.dp)) {
                    chatGroupViewModel.setMessageText(messageText + it)
                }
            }

            InputType.IMAGE -> {
                MultipleImagePicker {
                    if (it.isNotEmpty()) chatGroupViewModel.createMessage("", it, listOf())
                }
                GifPicker(
                    modifier = Modifier.height(300.dp)
                ) {
                    chatGroupViewModel.createMessage("", listOf(), listOf(it))
                }
            }

            InputType.TAKE_PICTURE -> {


                val file = remember { createImageFile(context) }
                val uri = remember {
                    FileProvider.getUriForFile(
                        context,
                        context.applicationContext.packageName + ".provider",
                        file
                    )
                }

                var capturedImageUri by remember {
                    mutableStateOf<Uri>(Uri.EMPTY)
                }

                val cameraLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.TakePicture()
                ) {
                    if (it) {
                        capturedImageUri = uri
                    } else {
                        chatGroupViewModel.setInputType(InputType.DEFAULT)
                    }
                }

                val permissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) {
                    if (it) {
                        Toast.makeText(context, "Permission Granted", Toast.LENGTH_SHORT).show()
                        cameraLauncher.launch(uri)
                    } else {
                        Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
                    }
                }


                LaunchedEffect(Unit) {
                    if (context.checkSelfPermission(android.Manifest.permission.CAMERA) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                        permissionLauncher.launch(android.Manifest.permission.CAMERA)
                    } else {
                        cameraLauncher.launch(uri)
                    }
                }

                if (capturedImageUri != Uri.EMPTY) {
                    Column {
                        AsyncImage(
                            model = capturedImageUri,
                            contentDescription = "Captured Image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                        )
                        Button(
                            onClick = {
                                chatGroupViewModel.createMessage(
                                    "",
                                    listOf(Pair(file, "image/jpeg")),
                                    listOf()
                                )
                            },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("Send")
                        }
                    }
                }

            }

            InputType.FILE -> {
                val sheetState = rememberModalBottomSheetState(
                    skipPartiallyExpanded = false,
                )
                ModalBottomSheet(
                    modifier = Modifier.fillMaxHeight(),
                    sheetState = sheetState,
                    onDismissRequest = {
                        chatGroupViewModel.setInputType(InputType.DEFAULT)
                    },
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MediaPicker(Modifier.fillMaxWidth()) {
                            chatGroupViewModel.setFiles(files + it)
                        }
                        files.forEach {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(it.first.name)
                                IconButton({ chatGroupViewModel.setFiles(files - it) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remove")
                                }
                            }
                        }
                    }
                }
            }

            InputType.MIC -> {

                var file by remember { mutableStateOf<Pair<java.io.File, String>?>(null) }
                val sheetState = rememberModalBottomSheetState(
                    skipPartiallyExpanded = false,
                )

                ModalBottomSheet(
                    modifier = Modifier.fillMaxHeight(),
                    sheetState = sheetState,
                    onDismissRequest = {
                        chatGroupViewModel.setInputType(InputType.DEFAULT)
                    },
                ) {
                    VoiceRecorder(Modifier.padding(8.dp)) {
                        file = it
                    }
                    Spacer(modifier = Modifier.height(20.dp))

                    if (file != null) {
                        Button(
                            onClick = {
                                chatGroupViewModel.createMessage("", listOf(file!!), listOf())
                                file = null
                            },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("Send")
                        }
                    }
                }
            }

            else -> {

            }
        }
    }
}


