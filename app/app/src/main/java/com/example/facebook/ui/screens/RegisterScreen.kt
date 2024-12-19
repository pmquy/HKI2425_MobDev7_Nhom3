package com.example.facebook.ui.screens

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.facebook.ui.FacebookScreen
import com.example.facebook.ui.components.ImagePicker
import kotlinx.coroutines.launch

@Composable
fun SignUpScreen(
    navController: NavController,
    registerViewModel: RegisterViewModel = viewModel(factory = RegisterViewModel.Factory),
) {
    val uiState by registerViewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val handleVerifyOtp: () -> Unit = {
        coroutineScope.launch {
            try {
                registerViewModel.otp()
                navController.navigate(FacebookScreen.LOGIN.name)
            } catch (e: Exception) {
                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (uiState.showOtpPage) {
                Text(
                    text = "Xác Thực Tài Khoản",
                    style = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Left,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 0.dp)
                )
                Text(
                    text = "Nhập mã OTP đã được gửi đến email của bạn",
                    style = TextStyle(fontSize = 16.sp),
                    color = Color.Gray,
                    textAlign = TextAlign.Left,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp)
                )
                OutlinedTextField(
                    value = uiState.otp,
                    onValueChange = registerViewModel.setOtp,
                    label = { Text("Mã OTP") },
                    placeholder = { Text("Nhập mã OTP") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
                Button(
                    onClick = handleVerifyOtp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    Text(text = "Xác nhận OTP")
                }
            } else if (uiState.currentStep == 1) {
                Text(
                    text = "Bắt Đầu Sử Dụng Ngay",
                    style = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Left,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp)
                )
                Text(
                    text = "ZoLA trò chuyện mọi nơi",
                    style = TextStyle(fontSize = 16.sp),
                    color = Color.Gray,
                    textAlign = TextAlign.Right,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    thickness = DividerDefaults.Thickness
                )
                OutlinedTextField(
                    value = uiState.firstName,
                    onValueChange = registerViewModel.setFirstName,
                    singleLine = true,
                    label = { Text("Tên người dùng") },
                    placeholder = { Text("Nhập tên người dùng") },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = {
                            focusManager.moveFocus(FocusDirection.Next)
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                )

                OutlinedTextField(
                    value = uiState.lastName,
                    onValueChange = registerViewModel.setLastName,
                    singleLine = true,
                    label = { Text("Họ và tên đệm") },
                    placeholder = { Text("Nhập họ và tên đệm người dùng") },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = {
                            focusManager.moveFocus(FocusDirection.Next)
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                )

                OutlinedTextField(
                    value = uiState.phoneNumber,
                    onValueChange = registerViewModel.setPhoneNumber,
                    label = { Text("Số điện thoại") },
                    placeholder = { Text("Nhập số điện thoại người dùng") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = {
                            focusManager.moveFocus(FocusDirection.Next)
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                )

                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = registerViewModel.setEmail,
                    label = { Text("Địa chỉ Email") },
                    placeholder = { Text("Nhập địa chỉ Email tài khoản") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = {
                            focusManager.moveFocus(FocusDirection.Next)
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                )

                OutlinedTextField(
                    value = uiState.password,
                    onValueChange = registerViewModel.setPassword,
                    label = { Text("Mật khẩu") },
                    placeholder = { Text("Nhập mật khẩu tài khoản") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = {
                            focusManager.moveFocus(FocusDirection.Next)
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
                Text(
                    text = "Mật khẩu cần tối thiểu 8 ký tự",
                    style = TextStyle(fontSize = 12.sp, color = Color.Gray),
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(start = 8.dp, bottom = 8.dp)
                )
                OutlinedTextField(
                    value = uiState.confirmPassword,
                    onValueChange = registerViewModel.setConfirmPassword,
                    label = { Text("Xác nhận mật khẩu") },
                    placeholder = { Text("Nhập lại mật khẩu") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
                Button(
                    onClick = { registerViewModel.setCurrentStep(2) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    Text(text = "Tiếp theo")
                }
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp)
                ) {
                    Text(text = "Đã có tài khoản?")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Đăng nhập ngay",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable {
                            navController.navigate(FacebookScreen.LOGIN.name)
                        }
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Chọn hình ảnh đại diện",
                        style = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Left,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Thể hiện cá tính của bạn",
                        style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                        color = Color.Gray,
                        textAlign = TextAlign.Left,
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .fillMaxWidth()
                    )
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 32.dp),
                        thickness = DividerDefaults.Thickness
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray),
                            contentAlignment = Alignment.Center
                        ) {
                            if (uiState.avatar != null) {
                                AsyncImage(
                                    model = uiState.avatar!!.first.toURI().toString(),
                                    contentDescription = "Avatar",
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(
                                    text = "No Image",
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    ImagePicker {
                        registerViewModel.setAvatar(it)
                    }
                    Button(
                        onClick = registerViewModel.handleRegister,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    ) {
                        Text(text = "Hoàn thành đăng ký")
                    }
                    Button(
                        onClick = { registerViewModel.setCurrentStep(1) },
                    ) {
                        Text(text = "Quay lại")
                    }
                }
            }
        }
    }

}

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun AvatarSelection(registerViewModel: RegisterViewModel) {
    val uiState by registerViewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Chọn hình ảnh đại diện",
            style = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Left,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "Thể hiện cá tính của bạn",
            style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold),
            color = Color.Gray,
            textAlign = TextAlign.Left,
            modifier = Modifier
                .padding(vertical = 8.dp)
                .fillMaxWidth()
        )
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 32.dp),
            thickness = DividerDefaults.Thickness
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.avatar != null) {
                    AsyncImage(
                        model = uiState.avatar!!.first.toURI().toString(),
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = "No Image",
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        ImagePicker {
            registerViewModel.setAvatar(it)
        }
        Button(
            onClick = registerViewModel.handleRegister,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Text(text = "Hoàn thành đăng ký")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSignUpScreen() {

}