package com.example.facebook.ui.screens

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.facebook.FacebookApplication
import com.example.facebook.model.User
import com.example.facebook.ui.FacebookScreen
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
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

        runBlocking {
            userViewModel.login("hieuma535@gmail.com", "mahieu1010")
        }
    }
//    single run pass test
//    @Test
//    fun friendProfileDisplaysTest() {
//        composeTestRule.setContent {
//            ProfileScreen(navController = navController,
//                userViewModel = userViewModel,
//                friendViewModel = friendsViewModel)
//        }
//        composeTestRule.waitForIdle()
//        composeTestRule.onNodeWithTag("TopInfo").assertExists().assertIsDisplayed()
//        composeTestRule.onNodeWithContentDescription("Back").assertExists().assertIsDisplayed()
//        composeTestRule.onNodeWithText("Thông tin cá nhân").assertExists().assertIsDisplayed()
//        composeTestRule.onNodeWithText("Chỉnh sửa thông tin").assertExists().assertIsDisplayed().assertIsEnabled()
//        composeTestRule.onNodeWithText("Đổi ảnh đại diện").assertExists().assertIsDisplayed().assertIsEnabled()
//        composeTestRule.onNodeWithText("Đổi mật khẩu").assertExists().assertIsDisplayed().assertIsEnabled()
//        composeTestRule.onNodeWithText("Đăng xuất").assertExists().assertIsDisplayed().assertIsEnabled()

//        composeTestRule.waitForIdle()
//        composeTestRule.onNodeWithText("Chỉnh sửa thông tin").assertExists().assertIsDisplayed().performClick()
//        composeTestRule.waitForIdle()
//        composeTestRule.onNodeWithText("Chỉnh sửa thông tin cá nhân").assertExists().assertIsDisplayed()
//        composeTestRule.onNodeWithText("Xác nhận").assertExists().assertIsDisplayed().assertIsEnabled()
//        composeTestRule.onNodeWithText("Hủy bỏ").assertExists().assertIsDisplayed().assertIsEnabled().performClick()
//        composeTestRule.waitForIdle()
//
//        composeTestRule.onNodeWithText("Đổi mật khẩu").assertExists().assertIsDisplayed().performClick()
//
//        composeTestRule.waitForIdle()
//
//        composeTestRule.onNodeWithText("Thay đổi mật khẩu").assertExists().assertIsDisplayed()
//        composeTestRule.onNodeWithText("Xác nhận").assertExists().assertIsDisplayed().assertIsEnabled()
//        composeTestRule.onNodeWithText("Hủy bỏ").assertExists().assertIsDisplayed().assertIsEnabled().performClick()
//
//        composeTestRule.waitForIdle()
//
//        composeTestRule.onNodeWithText("Đổi ảnh đại diện").assertExists().assertIsDisplayed().performClick()
//
//        composeTestRule.waitForIdle()
//
//        composeTestRule.onNodeWithText("Thay đổi ảnh đại diện").assertExists().assertIsDisplayed()
//        composeTestRule.onNodeWithText("Chọn ảnh").assertExists().assertIsDisplayed()
//        composeTestRule.onNodeWithText("Update").assertExists().assertIsDisplayed().assertIsEnabled()
//        composeTestRule.onNodeWithText("Cancel").assertExists().assertIsDisplayed().assertIsEnabled().performClick()
//
//        composeTestRule.waitForIdle()
//    }


    @Test
    fun textInfoTest() {
        composeTestRule.setContent {
            TextInfo(
                label = "Email:",
                value = userViewModel.uiState.value.user.email
            )
        }
        
        // Assertions are now outside the setContent block
        composeTestRule.onNodeWithText("Email:").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText(userViewModel.uiState.value.user.email).assertExists().assertIsDisplayed()
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
        val user = userViewModel.getUserById("672f79d772a9f050c372927d").value
        composeTestRule.setContent {
            FriendStatusSection(
                status = "suggest",
                user = user ?: User(),
                friendViewModel = friendsViewModel
            )
        }
            if (user != null) {
                composeTestRule.onNodeWithText("Bạn với ${user.firstName} là người lạ").assertExists().assertIsDisplayed()
            }
            composeTestRule.onNodeWithText("Thêm bạn bè").assertExists().assertIsDisplayed().assertIsEnabled()
    }

    @Test
    fun testNavigationToBackScreen_LastTest() {
        var currentRoute = ""
        composeTestRule.setContent {
            val navController = rememberNavController()
            NavHost(
                navController = navController,
                startDestination = FacebookScreen.HOME.name
            ) {
                composable(FacebookScreen.HOME.name) {
                    HomeScreen(userViewModel = userViewModel, navController = navController)
                }
                composable(FacebookScreen.PROFILE.name) {
                    ProfileScreen(
                        navController = navController,
                        userViewModel = userViewModel,
                        friendViewModel = friendsViewModel
                    )
                }
            }

            LaunchedEffect(Unit) {
                navController.navigate(FacebookScreen.PROFILE.name)
            }

            navController.addOnDestinationChangedListener { _, destination, _ ->
                currentRoute = destination.route.toString()
            }
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("Back").assertExists().assertIsDisplayed()

        composeTestRule.onNodeWithContentDescription("Back").performClick()

        composeTestRule.waitForIdle()

        assertTrue(currentRoute == FacebookScreen.HOME.name)

    }
}
