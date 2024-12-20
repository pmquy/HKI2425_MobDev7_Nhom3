package com.example.facebook.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.example.facebook.R
import com.example.facebook.model.User
import com.example.facebook.ui.FacebookScreen
import com.example.facebook.ui.components.File
import com.example.facebook.ui.components.ImagePicker
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userViewModel: UserViewModel = viewModel(factory = UserViewModel.Factory),
    friendViewModel: FriendsViewModel = viewModel(factory = FriendsViewModel.Factory),
    profileViewModel: ProfileViewModel = viewModel(factory = ProfileViewModel.Factory),
    navController: NavHostController,
) {
    val coroutineScope = rememberCoroutineScope()
    var id = navController.currentBackStackEntry?.arguments?.getString("id")
    if (id.isNullOrEmpty()) id = userViewModel.application.user._id
    LaunchedEffect(id) {
        profileViewModel.getUserById(id)
    }
    val user = profileViewModel.uiState.collectAsState().value.user
    val randomImageId by remember { mutableStateOf((1..4).random()) }
    val coverPhoto = painterResource(
        id = when (randomImageId) {
            1 -> R.drawable.pic1
            2 -> R.drawable.pic2
            3 -> R.drawable.pic3
            else -> R.drawable.pic4
        }
    )

    val handleLogout: () -> Unit = {
        coroutineScope.launch {
            try {
                userViewModel.logout()
                navController.navigate(FacebookScreen.LOGIN.name)
            } catch (e: Exception) {
                Toast.makeText(navController.context, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "${user?.firstName} ${user?.lastName}",
                        modifier = Modifier.testTag("TopInfo")
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                )
            )
        }
    ) { paddingValues ->
        if (user == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (!userViewModel.checkIfUser(id)) {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
            ) {
                Box(modifier = Modifier.height(200.dp)) {
                    Image(
                        painter = coverPhoto,
                        contentDescription = "Cover Photo",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentScale = ContentScale.Crop
                    )

                    // Ảnh đại diện
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        File(
                            user.avatar,
                            modifier = Modifier
                                .size(62.dp)
                                .clip(CircleShape),
                            allowOrigin = false
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${user.firstName} ${user.lastName}",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                shadow = Shadow(
                                    color = Color.Black,
                                    offset = Offset(2f, 2f),
                                    blurRadius = 4f
                                )
                            ),
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                    }
                }
                Text(
                    text = "Thông tin cá nhân",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .padding(start = 16.dp, top = 15.dp, bottom = 15.dp)
                        .fillMaxWidth()
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextInfo(label = "Email", value = user.email)
                    TextInfo(label = "Phone", value = user.phoneNumber)

                    FriendStatusSection(
                        status = user.friendStatus,
                        user = user,
                        friendViewModel = friendViewModel
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
            ) {
                Box(modifier = Modifier.height(340.dp)) {
                    Image(
                        painter = coverPhoto,
                        contentDescription = "Cover Photo",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentScale = ContentScale.Crop
                    )
                    // Ảnh đại diện
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 140.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        File(
                            user.avatar,
                            modifier = Modifier
                                .size(128.dp)
                                .clip(CircleShape),
                            allowOrigin = false
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${user.firstName} ${user.lastName}",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                shadow = Shadow(
                                    color = Color.Black,
                                    offset = Offset(2f, 2f),
                                    blurRadius = 4f
                                )
                            ),
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                Text(
                    text = "Thông tin cá nhân",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .padding(start = 16.dp, top = 8.dp, bottom = 15.dp)
                        .fillMaxWidth()
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextInfo(label = "Email", value = user.email)
                    TextInfo(label = "Phone", value = user.phoneNumber)

                }
                var showEditInfoDialog by remember { mutableStateOf(false) }
                var showEditPasswordDialog by remember { mutableStateOf(false) }
                var showEditImageDialog by remember { mutableStateOf(false) }
                Button(
                    onClick = { showEditInfoDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    Text("Chỉnh sửa thông tin")
                }
                Button(
                    onClick = { showEditImageDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    Text("Đổi ảnh đại diện")
                }
                Button(
                    onClick = { showEditPasswordDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    Text("Đổi mật khẩu")
                }

                if (showEditInfoDialog) {
                    EditInfoDialog(
                        user = user,
                        onDismiss = { showEditInfoDialog = false },
                        onUpdateInfo = { firstName, lastName, phoneNumber ->
                            coroutineScope.launch {
                                profileViewModel.handleUpdate(
                                    firstName = firstName,
                                    lastName = lastName,
                                    phoneNumber = phoneNumber
                                )
                                delay(500)
                                Log.wtf("Profile Screen", user.toString())
                            }
                        },
                    )
                }
                if (showEditPasswordDialog) {
                    EditPasswordDialog(
                        onDismiss = { showEditPasswordDialog = false },
                        onUpdatePassword = {
                            coroutineScope.launch {
                                profileViewModel.handleUpdate(password = it)
                            }
                            showEditPasswordDialog = false
                        }
                    )
                }
                if (showEditImageDialog) {
                    EditImageDialog(
                        user = user,
                        onDismiss = { showEditImageDialog = false },
                        onUpdateAvatar = {
                            coroutineScope.launch {
                                profileViewModel.handleUpdate(avatar = it)
                            }
                            showEditImageDialog = false
                        }
                    )
                }
                Button(
                    onClick = handleLogout,
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .align(Alignment.CenterHorizontally),
                    colors = ButtonDefaults.buttonColors(Color.Red)
                ) {
                    Text("Đăng xuất")
                }
            }
        }
    }
}

@Composable
fun EditInfoDialog(
    user: User,
    onDismiss: () -> Unit,
    onUpdateInfo: (firstName: String?, lastName: String?, phoneNumber: String) -> Unit,
) {
    var firstName by remember { mutableStateOf(user.firstName) }
    var lastName by remember { mutableStateOf(user.lastName) }
    var phoneNumber by remember { mutableStateOf(user.phoneNumber) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Chỉnh sửa thông tin cá nhân") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = firstName,
                    placeholder = { Text("Nhập tên người dùng") },
                    label = { Text("Tên người dùng") },
                    onValueChange = { firstName = it })

                OutlinedTextField(value = lastName,
                    placeholder = { Text("Nhập họ và tên đệm") },
                    label = { Text("Họ và tên đệm") },
                    onValueChange = { lastName = it })

                OutlinedTextField(value = phoneNumber,
                    label = { Text("Số điện thoại") },
                    placeholder = { Text("Số điện thoại") },
                    onValueChange = { phoneNumber = it })
            }
        },
        confirmButton = {
            Button(onClick = {
                onUpdateInfo(firstName, lastName, phoneNumber)
                onDismiss()
            }) {
                Text("Xác nhận")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Hủy bỏ")
            }
        }
    )
}

@Composable
fun EditPasswordDialog(
    onDismiss: () -> Unit,
    onUpdatePassword: (password: String) -> Unit
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Thay đổi mật khẩu") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = password,
                    label = { Text("Mật khẩu") },
                    placeholder = { Text("Mật khẩu") },
                    visualTransformation = PasswordVisualTransformation(),
                    onValueChange = { password = it }
                )
                OutlinedTextField(
                    value = confirmPassword,
                    label = { Text("Xác nhận mật khẩu") },
                    placeholder = { Text("Nhập lại mật khẩu để xác nhận") },
                    visualTransformation = PasswordVisualTransformation(),
                    onValueChange = { confirmPassword = it }
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                if (password != confirmPassword) {
                    Toast.makeText(context, "Mật khẩu không trùng khớp", Toast.LENGTH_SHORT).show()
                } else {
                    onUpdatePassword(password)
                    onDismiss()
                }
            }) {
                Text("Xác nhận")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Hủy bỏ")
            }
        }
    )
}

@Composable
fun EditImageDialog(
    user: User,
    onDismiss: () -> Unit,
    onUpdateAvatar: (avatar: Pair<File, String>) -> Unit
) {
    var avatar by remember { mutableStateOf<Pair<File, String>?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Thay đổi ảnh đại diện") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                avatar?.let {
                    AsyncImage(
                        model = it.first,
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
                ImagePicker { selectedAvatar ->
                    avatar = selectedAvatar
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                avatar?.let { onUpdateAvatar(it) }
                onDismiss()
            }) {
                Text("Update")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun TextInfo(label: String, value: String) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(3f)
            )
        }
        HorizontalDivider(modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp))
    }
}

@Composable
fun FriendStatusSection(
    status: String,
    user: User,
    friendViewModel: FriendsViewModel
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        when (status) {
            "friend" -> {
                Text("Bạn và ${user.firstName} đã là bạn bè")
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { friendViewModel.disfriend(user._id) }) {
                    Text("Hủy kết bạn")
                }
            }

            "send" -> {
                Text("Bạn đã gửi lời mời kết bạn tới ${user.firstName}")
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { friendViewModel.revoke(user._id) }) {
                    Text("Thu hồi lời mời")
                }
            }

            "request" -> {
                Text("${user.firstName} đã gửi cho bạn lời mời kết bạn")
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { friendViewModel.accept(user._id) }) {
                        Text("Chấp nhận")
                    }
                    Button(onClick = { friendViewModel.decline(user._id) }) {
                        Text("Từ chối")
                    }
                }
            }

            "suggest" -> {
                Text("Bạn với ${user.firstName} là người lạ")
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { friendViewModel.request(user._id) }) {
                    Text("Thêm bạn bè")
                }
            }
        }
    }
}

