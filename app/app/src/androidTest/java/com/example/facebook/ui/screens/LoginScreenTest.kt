package com.example.facebook.ui.screens

import androidx.compose.ui.test.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.facebook.ui.FacebookScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.test.core.app.ApplicationProvider
import junit.framework.TestCase.assertEquals
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.testing.TestNavHostController
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.Before

@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // Khởi tạo TestNavHostController
    private val navController = TestNavHostController(ApplicationProvider.getApplicationContext()).apply {
        navigatorProvider.addNavigator(ComposeNavigator())
    }

    private lateinit var mockUserViewModel: UserViewModel

    @Before
    fun setUp() {
        mockUserViewModel = mockk(relaxed = true)
    }

    @Test
    fun testLoginScreenDisplaysCorrectly() {
        composeTestRule.setContent {
            LoginScreen(navController = navController)
        }
        composeTestRule.onNodeWithText("Chào mừng quay trở lại!").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("Hãy nhập thông tin đăng nhập để truy cập vào tài khoản của bạn").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("Địa chỉ Email").assertExists().assertIsDisplayed().performClick()
        composeTestRule.onNodeWithText("Nhập địa chỉ Email tài khoản").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("Mật khẩu").assertExists().assertIsDisplayed().performClick()
        composeTestRule.onNodeWithText("Nhập mật khẩu tài khoản").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("Đăng nhập").assertExists().assertIsEnabled().assertIsDisplayed()
        composeTestRule.onNodeWithText("Chưa có tài khoản?").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("Đăng ký ngay").assertExists().assertIsDisplayed()
    }

    @Test
    fun testSuccessfulLogin() {
        composeTestRule.setContent {
            NavHost(
                navController = navController,
                startDestination = FacebookScreen.LOGIN.name
            ) {
                composable(FacebookScreen.LOGIN.name) {
                    LoginScreen(navController = navController, userViewModel = mockUserViewModel)
                }
                composable(FacebookScreen.HOME.name) {
                    HomeScreen(navController = navController, userViewModel = mockUserViewModel)
                }
            }
        }

        composeTestRule.onNodeWithText("Địa chỉ Email")
            .performTextInput("hieuma535@gmail.com")
        composeTestRule.onNodeWithText("Mật khẩu")
            .performTextInput("mahieu1010")
        composeTestRule.onNodeWithText("Đăng nhập").performClick()

        assertEquals(FacebookScreen.HOME.name, navController.currentDestination?.route)
        coVerify { mockUserViewModel.login("hieuma535@gmail.com", "mahieu1010") }
    }

    @Test
    fun testNavigationToSignUpScreen() {
        composeTestRule.setContent {
            NavHost(
                navController = navController,
                startDestination = FacebookScreen.LOGIN.name
            ) {
                composable(FacebookScreen.LOGIN.name) {
                    LoginScreen(navController = navController)
                }
                composable(FacebookScreen.SIGNUP.name) {
                    SignUpScreen(navController = navController)
                }
            }
        }
        composeTestRule.onNodeWithText("Đăng ký ngay").performClick()
        assertEquals(FacebookScreen.SIGNUP.name, navController.currentDestination?.route)
    }
}