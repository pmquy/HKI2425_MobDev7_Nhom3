package com.example.facebook.ui.screens

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.facebook.ui.FacebookScreen
import com.example.facebook.ui.components.ImagePicker
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.Q)
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
                navController.navigate(FacebookScreen.HOME.name)
            } catch (e: Exception) {
                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
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
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
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
                label = { Text("Tên người dùng") },
                placeholder = { Text("Nhập tên người dùng") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                textStyle = TextStyle(fontSize = 16.sp, color = Color.Black)
            )

            OutlinedTextField(
                value = uiState.lastName,
                onValueChange = registerViewModel.setLastName,
                label = { Text("Họ và tên đệm") },
                placeholder = { Text("Nhập họ và tên đệm người dùng") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                textStyle = TextStyle(fontSize = 16.sp, color = Color.Black)
            )

            OutlinedTextField(
                value = uiState.phoneNumber,
                onValueChange = registerViewModel.setPhoneNumber,
                label = { Text("Số điện thoại") },
                placeholder = { Text("Nhập số điện thoại người dùng") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                textStyle = TextStyle(fontSize = 16.sp, color = Color.Black)
            )

            OutlinedTextField(
                value = uiState.email,
                onValueChange = registerViewModel.setEmail,
                label = { Text("Địa chỉ Email") },
                placeholder = { Text("Nhập địa chỉ Email tài khoản") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                textStyle = TextStyle(fontSize = 16.sp, color = Color.Black)
            )

            OutlinedTextField(
                value = uiState.password,
                onValueChange = registerViewModel.setPassword,
                label = { Text("Mật khẩu") },
                placeholder = { Text("Nhập mật khẩu tài khoản") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )
            Text(
                text = "Mật khẩu cần tối thiểu 8 ký tự",
                style = TextStyle(fontSize = 12.sp, color = Color.Gray),
                modifier = Modifier.align(Alignment.Start).padding(start = 8.dp, bottom = 8.dp)
            )
            OutlinedTextField(
                value = uiState.confirmPassword,
                onValueChange = registerViewModel.setConfirmPassword,
                label = { Text("Xác nhận mật khẩu") },
                placeholder = { Text("Nhập lại mật khẩu") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
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
                modifier = Modifier.fillMaxWidth().padding(top = 64.dp)
            ) {
                Text(text = "Đã có tài khoản?", color = Color.Gray)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Đăng nhập ngay",
                    color = Color.Blue,
                    modifier = Modifier.clickable {
                        navController.navigate(FacebookScreen.LOGIN.name)
                    }
                )
            }
        } else {
            AvatarSelection(registerViewModel)
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
            modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth()
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