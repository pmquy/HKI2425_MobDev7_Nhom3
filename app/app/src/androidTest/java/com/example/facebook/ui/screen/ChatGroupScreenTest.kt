package com.example.facebook.ui.screen

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.printToLog
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.action.ViewActions.swipeUp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.facebook.data.ChatGroupRepository
import com.example.facebook.data.MessageRepository
import com.example.facebook.data.UserRepository
import com.example.facebook.fakeRepository.FakeChatGroup
import com.example.facebook.fakeRepository.FakeListMes
import com.example.facebook.fakeRepository.fakeUserRepository
import com.example.facebook.model.ChatGroup
import com.example.facebook.model.GetAllChatGroupsResponse
import com.example.facebook.model.GetMessagesResponse
import com.example.facebook.model.GetUsersResponse
import com.example.facebook.model.Member
import com.example.facebook.model.Message
import com.example.facebook.model.User
import com.example.facebook.ui.FacebookScreen
import com.example.facebook.ui.components.EmojiPicker
import com.example.facebook.ui.screens.AddMemberFromFriendList
import com.example.facebook.ui.screens.ChatGroupInfo
import com.example.facebook.ui.screens.ChatGroupScreen
import com.example.facebook.ui.screens.ChatGroupViewModel
import com.example.facebook.ui.screens.CreateMessage
import com.example.facebook.ui.screens.EditGroupInfo
import com.example.facebook.ui.screens.FriendsViewModel
import com.example.facebook.ui.screens.MemberListScreen
import com.example.facebook.ui.screens.MessageMain
import com.example.facebook.ui.screens.MessageUser
import com.example.facebook.ui.screens.MessageWrapper
import com.example.facebook.ui.screens.UserViewModel
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.just
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Response
import java.io.File


@RunWith(AndroidJUnit4::class)
class ChatGroupScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var navController: TestNavHostController
    private lateinit var userViewModel: UserViewModel
    private lateinit var chatGroupViewModel: ChatGroupViewModel
    private val mockFriendsViewModel = mockk<FriendsViewModel>(relaxed = true)

    @Before
    fun setUp() {
        userViewModel = UserViewModel(
            userRepository = fakeUserRepository(),
            socketRepository = mockk(relaxed = true),
            userPreferenceRepository = mockk(relaxed = true),
            application = ApplicationProvider.getApplicationContext()
        )

        chatGroupViewModel = ChatGroupViewModel(
            chatGroupRepository = FakeChatGroup(),
            userRepository = fakeUserRepository(),
            messageRepository = FakeListMes(),
            socketRepository = mockk(relaxed = true),
            application = ApplicationProvider.getApplicationContext()
        )

        navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        navController.navigatorProvider.addNavigator(ComposeNavigator())
    }

    @Test
    fun testChatGroupScreenDisplaysCorrectly() {
        composeTestRule.setContent {
            ChatGroupScreen(
                navController = navController,
                chatGroupViewModel = chatGroupViewModel,
                userViewModel = userViewModel
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(chatGroupViewModel.uiState.value.messageText).assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText(chatGroupViewModel.uiState.value.chatGroup.name).assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Search").assertExists().assertIsEnabled().assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Back").assertExists().assertIsEnabled().assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Info").assertExists().assertIsEnabled().assertIsDisplayed()
    }

    @Test
    fun testNavigationBackToHomeScreen() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            NavHost(
                navController = navController,
                startDestination = FacebookScreen.CHAT_GROUP.name
            ) {
                composable(FacebookScreen.CHAT_GROUP.name) {
                    ChatGroupScreen(
                        navController = navController,
                        chatGroupViewModel = chatGroupViewModel,
                        userViewModel = userViewModel
                    )
                }
                composable(FacebookScreen.HOME.name) {
                }
            }

            LaunchedEffect(Unit) {
                navController.navigate(FacebookScreen.HOME.name)
                delay(100)
                assertTrue(navController.currentBackStackEntry?.destination?.route == FacebookScreen.HOME.name)
                assertTrue(navController.previousBackStackEntry?.destination?.route == FacebookScreen.CHAT_GROUP.name)
                val canNavigateUp = navController.navigateUp()
                assertTrue(canNavigateUp)
                assertTrue(navController.currentBackStackEntry?.destination?.route == FacebookScreen.CHAT_GROUP.name)
            }
        }
        composeTestRule.waitForIdle()
    }


    // Trong test
    @Test
    fun testNavigationToGroupInfoScreen() {
        // Given
        val mockFriendsViewModel = mockk<FriendsViewModel>(relaxed = true)
        coEvery { mockFriendsViewModel.getFriends() } just Runs
        var screen = false
        // When
        composeTestRule.setContent {
            ChatGroupScreen(
                chatGroupViewModel = chatGroupViewModel,
                userViewModel = userViewModel,
                navController = navController
            )
            if(screen) {
                ChatGroupInfo(
                    chatGroupViewModel = chatGroupViewModel,
                    friendViewModel = mockFriendsViewModel,
                    onBack = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithContentDescription("Info").performClick()
        screen = true
        composeTestRule.onNodeWithText("Tùy chỉnh nhóm chat").assertIsDisplayed()
    }


    @Test
    fun testNavigationSearchScreen() {
        val chatGroupID = chatGroupViewModel.uiState.value.chatGroup._id

        composeTestRule.setContent {
            NavHost(
                navController = navController,
                startDestination = "${FacebookScreen.CHAT_GROUP.name}/${chatGroupID}"
            ) {
                composable("${FacebookScreen.CHAT_GROUP.name}/${chatGroupID}") { backStackEntry ->
                    ChatGroupScreen(
                        chatGroupViewModel = chatGroupViewModel,
                        userViewModel = userViewModel,
                        navController = navController
                    )
                }
                composable("${FacebookScreen.FIND_MESSAGE.name}/${chatGroupID}") { }
            }
        }

        // Navigate to the ChatGroupScreen
        composeTestRule.runOnUiThread {
            navController.navigate("${FacebookScreen.CHAT_GROUP.name}/$chatGroupID")
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("Search")
            .assertExists()
            .assertIsDisplayed()
            .performClick()

        composeTestRule.waitForIdle()

        composeTestRule.runOnUiThread {
            assertEquals("${FacebookScreen.FIND_MESSAGE.name}/$chatGroupID", navController.currentDestination?.route)
        }
    }

    @Test
    fun testChatGroupInfoDisplaysCorrectly() {
        composeTestRule.setContent {
            ChatGroupInfo(
                friendViewModel = mockFriendsViewModel,
                chatGroupViewModel = chatGroupViewModel,
                onBack = {}
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Tùy chỉnh nhóm chat").assertExists()

        composeTestRule.onNodeWithContentDescription("Back").assertExists()

        composeTestRule.onNodeWithText("Thay đổi ảnh và tên nhóm").assertExists()

        composeTestRule.onNodeWithText("Xem thành viên nhóm").assertExists()

        composeTestRule.onNodeWithText("Thêm thành viên từ danh sách bạn bè").assertExists()

        composeTestRule.onRoot().performTouchInput { swipeUp() }

        composeTestRule.onNodeWithText("Tùy chỉnh nhóm chat").assertIsDisplayed()

        composeTestRule.onNodeWithContentDescription("Back").assertIsDisplayed()

        composeTestRule.onNodeWithText("Thay đổi ảnh và tên nhóm").assertIsDisplayed()

        composeTestRule.onNodeWithText("Xem thành viên nhóm").assertIsDisplayed()

        composeTestRule.onNodeWithText("Thêm thành viên từ danh sách bạn bè").assertIsDisplayed()

        composeTestRule.onRoot().printToLog("ChatGroupInfoTree")
    }

    @Test
    fun testNavigationEditGroupInfo() {
        var screen = false
        composeTestRule.setContent {
            ChatGroupInfo(
                friendViewModel = mockFriendsViewModel,
                chatGroupViewModel = chatGroupViewModel,
                onBack = {}
            )
            if(screen) {
                EditGroupInfo(
                    chatGroup = chatGroupViewModel.uiState.value.chatGroup,
                    onDismiss = { },
                    onUpdateInfo = {userName, avatar -> chatGroupViewModel.handleUpdate(userName, avatar) }
                )
            }
        }
        composeTestRule.onNodeWithTag("Change").performClick()
        screen = true
        composeTestRule.onNodeWithText("Chỉnh sửa thông tin nhóm chat").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("Chọn ảnh").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("Thay đổi ảnh nhóm").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("Thay đổi tên nhóm").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("Tên nhóm chat").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText(chatGroupViewModel.uiState.value.chatGroup.name).assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("Xác nhận").assertExists().assertIsDisplayed().assertIsDisplayed()
        composeTestRule.onNodeWithText("Hủy bỏ").assertExists().assertIsDisplayed().assertIsDisplayed()
    }


    @Test
    fun testNavigationMemberListScreen() {
        var screen = false
        composeTestRule.setContent {
            ChatGroupInfo(
                friendViewModel = mockFriendsViewModel,
                chatGroupViewModel = chatGroupViewModel,
                onBack = {}
            )
            if(screen) {
                MemberListScreen(
                    chatGroupViewModel = chatGroupViewModel,
                    onBack = {}
                )
            }
        }
        composeTestRule.onNodeWithTag("Look").performClick()
        screen = true
        composeTestRule.onNodeWithText("Xem thành viên nhóm chat").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("Xác nhận").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("Reload").assertExists().assertIsDisplayed()
    }

    @Test
    fun testNavigationAddMemberFromFriendList() {
        var screen = false
        composeTestRule.setContent {
            ChatGroupInfo(
                friendViewModel = mockFriendsViewModel,
                chatGroupViewModel = chatGroupViewModel,
                onBack = {}
            )
            if(screen) {
                AddMemberFromFriendList(
                    friendViewModel = mockFriendsViewModel,
                    chatGroupViewModel = chatGroupViewModel,
                    onDismiss = {}
                )
            }
        }
        composeTestRule.onNodeWithTag("Add").performClick()
        screen = true
        composeTestRule.onNodeWithText("Thêm bạn bè vào nhóm chat").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("Xác nhận").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("Reload").assertExists().assertIsDisplayed()
    }

    @Test
    fun testMessageWrapperCorrectly() {
        composeTestRule.setContent {
            MessageWrapper(
                message = chatGroupViewModel.uiState.value.messages.firstOrNull() ?: Message(),
                user = userViewModel.uiState.value.user,
                isMine = true,
                onClick = {}
            )
        }
        chatGroupViewModel.uiState.value.messages.firstOrNull()?.message?.let { messageText ->
            composeTestRule.onNodeWithText(messageText).assertExists().assertIsDisplayed()
        }
    }

    @Test
    fun testMessageUserDisplaysCorrectly() {
        composeTestRule.setContent {
            MessageUser(
                user = userViewModel.uiState.value.user,
                onClick = {}
            )
        }
        // If you want to check for specific UI elements or layouts, you can use testTags
        composeTestRule.onNodeWithTag("Avatar").assertExists().assertIsDisplayed()
    }

    @Test
    fun testMessageMainDisplaysCorrectly() {
        val fakeListMes = FakeListMes()
        val message = runBlocking { fakeListMes.getAll().body()?.firstOrNull() ?: Message() }
        composeTestRule.setContent {
            MessageMain(
                user = userViewModel.uiState.value.user,
                message = message,
                isMine = false
            )
        }
        composeTestRule.onNodeWithText(userViewModel.uiState.value.user.firstName + " " +
                userViewModel.uiState.value.user.lastName).assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText(message.message).assertExists().assertIsDisplayed()
    }

    @Test
    fun createMessageDisplaysCorrectly() {
        composeTestRule.setContent {
            CreateMessage(
                chatGroupViewModel = chatGroupViewModel
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("ADDFILE").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithTag("CAM").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithTag("IMG").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithTag("MIC").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithTag("messageText").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithTag("EMOTICON").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithTag("SENDmes").assertExists().assertIsNotEnabled()
    }

    @Test
    fun testSENDmesEnabled() {
        composeTestRule.setContent {
            CreateMessage(
                chatGroupViewModel = chatGroupViewModel
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("messageText").performTextInput("Test message")
        composeTestRule.onNodeWithTag("SENDmes").assertExists().assertIsEnabled()
    }

    @Test
    fun testEmojiPicker() {
        var screen = false
        composeTestRule.setContent {
            CreateMessage(
                chatGroupViewModel = chatGroupViewModel
            )
            if(screen) {
                EmojiPicker(
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("EMOTICON").performClick()
        screen = true
        composeTestRule.onNodeWithText(chatGroupViewModel.uiState.value.messageText).assertExists().assertIsDisplayed()
    }

}