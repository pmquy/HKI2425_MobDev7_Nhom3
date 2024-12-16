package com.example.facebook.ui.screen

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.facebook.FacebookApplication
import com.example.facebook.data.SocketRepository
import com.example.facebook.fakeRepository.FakeChatGroup
import com.example.facebook.model.Friend
import com.example.facebook.model.User
import com.example.facebook.ui.FacebookScreen
import com.example.facebook.ui.screens.*
import io.mockk.mockk
import kotlinx.coroutines.delay
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue


@RunWith(AndroidJUnit4::class)
class ProfileScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var navController: TestNavHostController
    private lateinit var userViewModel: UserViewModel
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var friendsViewModel: FriendsViewModel

    @Before
    fun setup() {
        navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        val application = ApplicationProvider.getApplicationContext<FacebookApplication>()

        userViewModel = UserViewModel(
            userRepository = application.container.userRepository,
            socketRepository = application.container.socketRepository,
            userPreferenceRepository = application.container.userPreferenceRepository,
            application = application
        )

        homeViewModel = HomeViewModel(
            chatGroupRepository = application.container.chatGroupRepository,
        )

        friendsViewModel = FriendsViewModel(
            friendRepository = application.container.friendRepository,
            application = ApplicationProvider.getApplicationContext()
        )

        // Perform login
        runBlocking {
            userViewModel.login("hieuma535@gmail.com", "mahieu1010")
        }
    }

    @Test
    fun testHomeScreenDisplaysCorrectly() {
        composeTestRule.setContent {
            ProfileScreen(navController = navController,
                userViewModel = userViewModel,
                friendViewModel = friendsViewModel)
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(userViewModel.uiState.value.user.firstName + " " + userViewModel.uiState.value.user.lastName).assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText(userViewModel.uiState.value.user.email).assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("Thông tin cá nhân").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText(userViewModel.uiState.value.user.phoneNumber).assertExists().assertIsDisplayed()
    }

    @Test
    fun testNavigationToBackScreen() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            NavHost(
                navController = navController,
                startDestination = FacebookScreen.PROFILE.name
            ) {
                composable("${FacebookScreen.PROFILE.name}/${userViewModel.uiState.value.user._id}") {
                    ProfileScreen(
                        navController = navController,
                        userViewModel = userViewModel,
                        friendViewModel = friendsViewModel
                    )
                }
                composable(FacebookScreen.HOME.name) {
                }
            }

            LaunchedEffect(Unit) {
                navController.navigate(FacebookScreen.HOME.name)
                delay(100)
                assertTrue(navController.currentBackStackEntry?.destination?.route == FacebookScreen.HOME.name)
            }
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun editInfoDialogTest() {
        composeTestRule.setContent {    
            EditInfoDialog(
                user = userViewModel.uiState.value.user,
                onDismiss = { },
                onUpdateInfo = { firstName, lastName, phoneNumber ->
                }
            )
            composeTestRule.onNodeWithText("Chỉnh sửa thông tin cá nhân").assertExists().assertIsDisplayed()
            composeTestRule.onNodeWithText("Nhập tên người dùng").assertExists().assertIsDisplayed().performClick()
            composeTestRule.onNodeWithText("Tên người dùng").assertExists().assertIsDisplayed()
            composeTestRule.onNodeWithText("Nhập họ và tên đệm").assertExists().assertIsDisplayed().performClick()
            composeTestRule.onNodeWithText("Họ và tên đệm").assertExists().assertIsDisplayed()
            composeTestRule.onNodeWithText("Số điện thoại").assertExists().assertIsDisplayed().performClick()
            composeTestRule.onNodeWithText("Xác nhận").assertExists().assertIsDisplayed().assertIsEnabled()
            composeTestRule.onNodeWithText("Hủy bỏ").assertExists().assertIsDisplayed().assertIsEnabled()
        }
    }

    @Test
    fun editPasswordDialogTest() {
        composeTestRule.setContent {
            EditPasswordDialog(
                onDismiss = { },
                onUpdatePassword = { newPassword ->
                }
            )
            composeTestRule.onNodeWithText("Thay đổi mật khẩu").assertExists().assertIsDisplayed()
            composeTestRule.onNodeWithText("Mật khẩu ").assertExists().assertIsDisplayed()
            composeTestRule.onNodeWithText("Xác nhận mật khẩu").assertExists().assertIsDisplayed().performClick()
            composeTestRule.onNodeWithText("Nhập lại mật khẩu để xác nhận").assertExists().assertIsDisplayed()
            composeTestRule.onNodeWithText("Xác nhận").assertExists().assertIsDisplayed().assertIsEnabled()
            composeTestRule.onNodeWithText("Hủy bỏ").assertExists().assertIsDisplayed().assertIsEnabled()
        }
    }

    @Test
    fun editImageDialogTest() {
        composeTestRule.setContent {
            EditImageDialog(
                user = userViewModel.uiState.value.user,
                onDismiss = { },
                onUpdateAvatar = { imageUri ->
                }
            )
            composeTestRule.onNodeWithText("Thay đổi ảnh đại diện").assertExists().assertIsDisplayed()
            composeTestRule.onNodeWithText("Update").assertExists().assertIsDisplayed().assertIsEnabled()
            composeTestRule.onNodeWithText("Cancel").assertExists().assertIsDisplayed().assertIsEnabled()
        }
    }

    @Test
    fun textInfoTest() {
        composeTestRule.setContent {
            TextInfo(
                label = "Title",
                value = "Content"
            )
            composeTestRule.onNodeWithText("Title").assertExists().assertIsDisplayed()}
            composeTestRule.onNodeWithText("Content").assertExists().assertIsDisplayed()
    }

    @Test
    fun friendStatusSectionFriendTest() {
        composeTestRule.setContent {
        friendsViewModel.uiState.value.friends.forEach {
                if (it.status == "accepted") {
                    var itUser = ""
                    itUser = if (userViewModel.uiState.value.user._id == it.from) {
                        it.to
                    } else {
                        it.from
                    }
            val user = userViewModel.getUserById(itUser).collectAsState(initial = null).value
            FriendStatusSection(
                status = "friend",
                user = user ?: User(),
                friendViewModel = friendsViewModel
            )
                    if (user != null) {
                        composeTestRule.onNodeWithText("Bạn và ${user.firstName} đã là bạn bè").assertExists().assertIsDisplayed()
                        composeTestRule.onNodeWithText("Hủy kết bạn").assertExists().assertIsDisplayed().assertIsEnabled()
                    }
            }
        }
    }
    }

    @Test
    fun friendStatusSectionRequestOrSendTest() {
        composeTestRule.setContent {
            friendsViewModel.uiState.value.friends.forEach {
                if (it.status == "pending") {
                    var itUser = ""
                    if (userViewModel.uiState.value.user._id == it.from) {
                        val user = userViewModel.getUserById(it.from).collectAsState(initial = null).value
                        FriendStatusSection(
                            status = "request",
                            user = user ?: User(),
                            friendViewModel = friendsViewModel
                        )
                        if (user != null) {
                            composeTestRule.onNodeWithText("${user.firstName} đã gửi cho bạn lời mời kết bạn").assertExists().assertIsDisplayed()
                            composeTestRule.onNodeWithText("Chấp nhận").assertExists().assertIsDisplayed().assertIsEnabled()
                            composeTestRule.onNodeWithText("Từ chối").assertExists().assertIsDisplayed().assertIsEnabled()
                        }
                    } else {
                        val user = userViewModel.getUserById(itUser).collectAsState(initial = null).value
                        FriendStatusSection(
                            status = "send",
                            user = user ?: User(),
                            friendViewModel = friendsViewModel
                        )
                        if (user != null) {
                            composeTestRule.onNodeWithText("Bạn đã gửi lời mời kết bạn tới ${user.firstName}").assertExists().assertIsDisplayed()
                            composeTestRule.onNodeWithText("Thu hồi lời mời").assertExists().assertIsDisplayed().assertIsEnabled()
                        }
                    }

                }
            }
        }
    }
    @Test
    fun friendStatusSectionSuggestTest() {
        composeTestRule.setContent {
            val user = userViewModel.getUserById("672f79d772a9f050c372927d").value
            FriendStatusSection(
                status = "suggest",
                user = user ?: User(),
                friendViewModel = friendsViewModel
            )
            if (user != null) {
                composeTestRule.onNodeWithText("Bạn với ${user.firstName} là người lạ").assertExists().assertIsDisplayed()
            }
            composeTestRule.onNodeWithText("Thêm bạn bè").assertExists().assertIsDisplayed().assertIsEnabled()
        }
    }
}