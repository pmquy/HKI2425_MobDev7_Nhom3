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
            userRepository = fakeUserRepository(),
            socketRepository = mockk<SocketRepository>(relaxed = true),
            userPreferenceRepository = mockk(relaxed = true),
            application = ApplicationProvider.getApplicationContext()
        )

        homeViewModel = HomeViewModel(
            chatGroupRepository = FakeChatGroup(),
        )

        friendsViewModel = FriendsViewModel(
            friendRepository = fakeFriendsRepository(),
            application = ApplicationProvider.getApplicationContext()
        )

        // Perform login
        runBlocking {
            userViewModel.login("ssbkss1010@gmail.com", "mahieu1010")
            composeTestRule.waitForIdle()
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
    fun EditInfoDialogTest() {
        composeTestRule.setContent {    
            EditInfoDialog(
                user = userViewModel.uiState.value.user,
                onDismiss = { },
                onUpdateInfo = { firstName, lastName, phoneNumber ->
                    // Handle the update info logic here
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
    fun EditPasswordDialogTest() {
        composeTestRule.setContent {
            EditPasswordDialog(
                onDismiss = { },
                onUpdatePassword = { newPassword ->
                    // Handle the update password logic here
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
    fun EditImageDialogTest() {
        composeTestRule.setContent {
            EditImageDialog(
                user = userViewModel.uiState.value.user,
                onDismiss = { },
                onUpdateAvatar = { imageUri ->
                    // Handle the update image logic here
                }
            )
            composeTestRule.onNodeWithText("Thay đổi ảnh đại diện").assertExists().assertIsDisplayed()
            composeTestRule.onNodeWithText("Update").assertExists().assertIsDisplayed().assertIsEnabled()
            composeTestRule.onNodeWithText("Cancel").assertExists().assertIsDisplayed().assertIsEnabled()
        }
    }

    @Test
    fun TextInfoTest() {
        composeTestRule.setContent {
            TextInfo(
                label = "Title",
                value = "Content"
            )
            composeTestRule.onNodeWithText("Title").assertExists().assertIsDisplayed()}
            composeTestRule.onNodeWithText("Content").assertExists().assertIsDisplayed()
    }

    @Test
    fun FriendStatusSectionFriendTest() {
        composeTestRule.setContent {
            friendsViewModel.getFriends()
            val status = friendsViewModel.uiState.value.friends
            val user = userViewModel.getUserById(status[0].from).collectAsState(initial = null).value
            FriendStatusSection(
                status = status[0].status,
                user = user ?: User(), // Provide a default User object if user is null
                friendViewModel = friendsViewModel
            )
            composeTestRule.onNodeWithText("Bạn và test2 đã là bạn bè").assertExists().assertIsDisplayed()
            composeTestRule.onNodeWithText("Hủy kết bạn").assertExists().assertIsDisplayed().assertIsEnabled()
        }
    }
    @Test
    fun FriendStatusSectionSendTest() {
        composeTestRule.setContent {
            FriendStatusSection(
                status = "send",
                user = User(
                    _id = "1",
                    firstName = "test4",
                    lastName = "yes",
                    email = "jjhajhe@m.o"),
                friendViewModel = friendsViewModel
            )
            composeTestRule.onNodeWithText("Bạn đã gửi lời mời kết bạn tới test4").assertExists().assertIsDisplayed().assertIsEnabled()
            composeTestRule.onNodeWithText("Thu hồi lời mời").assertExists().assertIsDisplayed().assertIsEnabled()
        }
    }

    @Test
    fun FriendStatusSectionRequestTest() {
        composeTestRule.setContent {
            FriendStatusSection(
                status = "request",
                user = User(
                    _id = "3",
                    firstName = "test",
                    lastName = "no",
                    email = "jjhajhaa2e@m.o"
                ),
                friendViewModel = friendsViewModel
            )
            composeTestRule.onNodeWithText("test đã gửi cho bạn lời mời kết bạn").assertExists().assertIsDisplayed()
            composeTestRule.onNodeWithText("Chấp nhận").assertExists().assertIsDisplayed().assertIsEnabled()
            composeTestRule.onNodeWithText("Từ chối").assertExists().assertIsDisplayed().assertIsEnabled()
        }
    }
    @Test
    fun FriendStatusSectionSuggestTest() {
        composeTestRule.setContent {
            FriendStatusSection(
                status = "suggest",
                user = User(
                    _id = "5",
                    firstName = "test3",
                    lastName = "no",
                    email = "jjhaa2e@m.5o"
                ),
                friendViewModel = friendsViewModel
            )
            composeTestRule.onNodeWithText("Bạn với test3 là người lạ").assertExists().assertIsDisplayed()
            composeTestRule.onNodeWithText("Thêm bạn bè").assertExists().assertIsDisplayed().assertIsEnabled()
        }
    }
}