package com.example.facebook.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.facebook.FacebookApplication
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SignUpScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var navController: TestNavHostController
    private lateinit var registerViewModel: RegisterViewModel

    @Before
    fun setUp() {
        navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        val application = ApplicationProvider.getApplicationContext<FacebookApplication>()

        registerViewModel = RegisterViewModel(
            userRepository = application.container.userRepository,
            application  = application
        )
    }

//    @Test
//    fun testSignUpScreenDisplaysCorrectly() {
//        composeTestRule.setContent {
//            SignUpScreen(
//                navController = navController,
//                registerViewModel = registerViewModel
//            )
//        }
//        composeTestRule.waitUntil(5000) {
//            composeTestRule.onNodeWithText("Bắt Đầu Sử Dụng Ngay").isDisplayed()
//        }
//
//        composeTestRule.onNodeWithText("Bắt Đầu Sử Dụng Ngay").assertIsDisplayed()
//        composeTestRule.onNodeWithText("ZoLA trò chuyện mọi nơi").assertIsDisplayed()
//        composeTestRule.onNodeWithText("Tên người dùng").assertIsDisplayed().performClick()
//        composeTestRule.onNodeWithText("Nhập tên người dùng").assertIsDisplayed()
//        composeTestRule.onNodeWithText("Họ và tên đệm").assertIsDisplayed().performClick()
//        composeTestRule.onNodeWithText("Nhập họ và tên đệm người dùng").assertIsDisplayed()
//        composeTestRule.onNodeWithText("Số điện thoại").assertIsDisplayed().performClick()
//        composeTestRule.onNodeWithText("Nhập số điện thoại người dùng").assertIsDisplayed()
//        composeTestRule.onNodeWithText("Địa chỉ Email").assertIsDisplayed().performClick()
//        composeTestRule.onNodeWithText("Nhập địa chỉ Email tài khoản").assertIsDisplayed()
//        composeTestRule.onNodeWithText("Mật khẩu").assertIsDisplayed().performClick()
//        composeTestRule.onNodeWithText("Nhập mật khẩu tài khoản").assertIsDisplayed()
//        composeTestRule.onNodeWithText("Mật khẩu cần tối thiểu 8 ký tự").assertIsDisplayed().performClick()
//        composeTestRule.onNodeWithText("Xác nhận mật khẩu").assertIsDisplayed().performClick()
//        composeTestRule.onNodeWithText("Nhập lại mật khẩu").assertIsDisplayed()
//        composeTestRule.onNodeWithText("Tiếp theo").assertIsDisplayed().assertIsEnabled()
//        composeTestRule.onNodeWithText("Đã có tài khoản?").assertIsDisplayed()
//        composeTestRule.onNodeWithText("Đăng nhập ngay").assertIsDisplayed().assertIsEnabled()
//    }
//
//    @Test
//    fun navigateToLoginScreen() {
//        var currentRoute = ""
//        composeTestRule.setContent {
//            val navController = rememberNavController()
//            val startDestination = FacebookScreen.SIGNUP.name
//
//            navController.addOnDestinationChangedListener { _, destination, _ ->
//                currentRoute = destination.route.toString()
//            }
//
//            NavHost(
//                navController = navController,
//                startDestination = startDestination
//            ) {
//                composable(FacebookScreen.SIGNUP.name) {
//                    SignUpScreen(navController = navController)
//                }
//                composable(FacebookScreen.LOGIN.name) {
//                    LoginScreen(navController = navController)
//                }
//            }
//        }
//
//        composeTestRule.waitUntil(5000) {
//            composeTestRule.onNodeWithText("Đăng nhập ngay").isDisplayed()
//        }
//        composeTestRule.onNodeWithText("Đăng nhập ngay").performClick()
//        composeTestRule.waitForIdle()
//
//
//        assert(currentRoute == FacebookScreen.LOGIN.name) {
//            "Expected route to be ${FacebookScreen.LOGIN.name}, but was $currentRoute"
//        }
//    }
//
//
//    @Test
//    fun navigateToAvatarSelection() {
//        composeTestRule.setContent {
//            SignUpScreen(navController = navController)
//        }
//        composeTestRule.waitUntil(5000) {
//            composeTestRule.onNodeWithText("Tiếp theo").isDisplayed()
//        }
//        composeTestRule.onNodeWithText("Tiếp theo").performClick()
//        composeTestRule.waitForIdle()
//        composeTestRule.onNodeWithText("Chọn hình ảnh đại diện").assertExists().assertIsDisplayed()
//        composeTestRule.onNodeWithText("Thể hiện cá tính của bạn").assertIsDisplayed()
//        composeTestRule.onNodeWithText("Chọn ảnh").assertIsDisplayed().assertIsEnabled()
//        composeTestRule.onNodeWithText("Hoàn thành đăng ký").assertIsDisplayed().assertIsEnabled()
//    }

    @Test
    fun testAvatarSelectionDisplaysCorrectly() {
        composeTestRule.setContent {
            AvatarSelection(
                registerViewModel = registerViewModel
            )
        }

        composeTestRule.onNodeWithText("Chọn hình ảnh đại diện").assertIsDisplayed()
    }

}