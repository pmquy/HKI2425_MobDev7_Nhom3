package com.example.facebook.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.facebook.ui.FacebookScreen
import kotlinx.coroutines.launch

//@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(userViewModel: UserViewModel = viewModel(factory = UserViewModel.Factory), navController: NavController) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    val handleLogin: () -> Unit = {
        coroutineScope.launch {
            try {
                userViewModel.login(email, password)
                navController.navigate(FacebookScreen.HOME.name)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
            .padding(horizontal = 16.dp)
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Chào mừng quay trở lại!",
            style = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Left,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "Hãy nhập thông tin đăng nhập để truy cập vào tài khoản của bạn",
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
        Spacer(modifier = Modifier.height(16.dp))
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
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        )

        Button(
            onClick = handleLogin,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Text("Đăng nhập")
        }

        Text(
            text = "Quên mật khẩu?",
            color = Color.Blue,
            textAlign = TextAlign.Right,
            modifier = Modifier
                .clickable { /* Handle forgot password action */ }
                .padding(bottom = 32.dp)
                .fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(96.dp))
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Chưa có tài khoản?", color = Color.Gray)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Đăng ký ngay",
                color = Color.Blue,
                modifier = Modifier.clickable {
                    // Navigate to the Sign Up screen
                    navController.navigate(FacebookScreen.SIGNUP.name)
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {

}