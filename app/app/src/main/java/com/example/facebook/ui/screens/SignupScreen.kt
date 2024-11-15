package com.example.facebook.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.facebook.ui.FacebookScreen
import kotlinx.coroutines.launch

@Composable
fun SignUpScreen(
    navController: NavController,
    userViewModel: UserViewModel = viewModel(factory = UserViewModel.Factory),
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val isPasswordError = remember { mutableStateOf(false) }
    var otp by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val handleSignUp: () -> Unit = {
        if (password == confirmPassword) {
            coroutineScope.launch {
                try {
                    userViewModel.signUp(firstName = "", lastName = username, email, password)
                    isOtpSent = true
                } catch (e: Exception) {
                    // Handle error
                }
            }
        } else {
            isPasswordError.value = true
        }
    }
    val handleVerifyOtp: () -> Unit = {
        coroutineScope.launch {
            try {
                userViewModel.verifyOtp(email, otp)
                navController.navigate(FacebookScreen.HOME.name)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (!isOtpSent) {
            Text(
                text = "Bắt Đầu Sử Dụng Ngay",
                style = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Left,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 0.dp)
            )
            Text(
                text = "ZoLA trò chuyện mọi nơi",
                style = TextStyle(fontSize = 16.sp),
                color = Color.Gray,
                textAlign = TextAlign.Right,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            )

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Tên tài khoản") },
                placeholder = { Text("Nhập tên người dùng hiển thị") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Địa chỉ Email") },
                placeholder = { Text("Nhập địa chỉ Email tài khoản") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Mật khẩu") },
                placeholder = { Text("Nhập mật khẩu tài khoản") },
                visualTransformation = PasswordVisualTransformation(),
                isError = isPasswordError.value,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )
            if(!isPasswordError.value) {
                Text(
                    text = "Mật khẩu cần tối thiểu 8 ký tự",
                    style = TextStyle(fontSize = 12.sp, color = Color.Gray),
                    modifier = Modifier.align(Alignment.Start).padding(start = 8.dp, bottom = 8.dp)
                )
            } else {
                Text(
                    text = "Mật khẩu không khớp",
                    style = TextStyle(fontSize = 12.sp, color = Color.Red),
                    modifier = Modifier.align(Alignment.Start).padding(start = 8.dp, bottom = 8.dp)
                )
            }
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Xác nhận mật khẩu") },
                placeholder = { Text("Nhập lại mật khẩu") },
                visualTransformation = PasswordVisualTransformation(),
                isError = isPasswordError.value,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )

            Button(
                onClick = handleSignUp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Text(text = "Đăng ký ngay")
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
                textAlign = TextAlign.Right,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            )
            OutlinedTextField(
                value = otp,
                onValueChange = { otp = it },
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
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSignUpScreen() {
    val navController = rememberNavController()
    SignUpScreen(
        navController = navController,
    )
}