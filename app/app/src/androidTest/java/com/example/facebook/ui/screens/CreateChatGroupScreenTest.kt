package com.example.facebook.ui.screens

import android.util.Log
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.facebook.FacebookApplication
import com.example.facebook.data.ChatGroupRepository
import com.example.facebook.data.FriendRepository
import com.example.facebook.data.UserRepository
import com.example.facebook.ui.FacebookScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CreateChatGroupScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var navController: TestNavHostController
    private lateinit var userRepository: UserRepository
    private lateinit var friendRepository: FriendRepository
    private lateinit var userViewModel: UserViewModel
    private lateinit var friendsViewModel: FriendsViewModel
    private lateinit var createChatGroupViewModel: CreateChatGroupViewModel
    private lateinit var chatGroupRepository: ChatGroupRepository

    @Before
    fun setUp() {
        navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        val application = ApplicationProvider.getApplicationContext<FacebookApplication>()

        createChatGroupViewModel = CreateChatGroupViewModel(
            chatGroupRepository = application.container.chatGroupRepository,
            application = application
        )

        friendsViewModel = FriendsViewModel(
            friendRepository = application.container.friendRepository,
            application = application
        )

        userViewModel = UserViewModel(
            userRepository = application.container.userRepository,
            socketRepository = application.container.socketRepository,
            userPreferenceRepository = application.container.userPreferenceRepository,
            application = application
        )
        runBlocking {
            userViewModel.login("hieuma535@gmail.com", "mahieu1010")
        }
    }

    @Test
    fun testCreateChatGroupScreenDisplaysCorrectly() {
        composeTestRule.setContent {
            CreateChatGroupScreen(
                navController = navController,
                userViewModel = userViewModel,
                friendViewModel = friendsViewModel,
                createChatGroupViewModel = createChatGroupViewModel
            )
        }
        composeTestRule.onNodeWithText("Create new group chat").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Back").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("Group name").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("Chọn ảnh").assertExists().assertIsDisplayed().assertIsEnabled()
        composeTestRule.onNodeWithText("Create").assertExists().assertIsDisplayed().assertIsEnabled()
    }

    @Test
    fun testNavigationToBackScreen() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            NavHost(
                navController = navController,
                startDestination = FacebookScreen.CREATE_CHAT_GROUP.name
            ) {
                composable(FacebookScreen.CREATE_CHAT_GROUP.name) {
                    CreateChatGroupScreen(
                        navController = navController,
                        userViewModel = userViewModel,
                        friendViewModel = friendsViewModel,
                        createChatGroupViewModel = createChatGroupViewModel
                    )
                }
                composable(FacebookScreen.HOME.name) {
                }
            }
            LaunchedEffect(Unit) {
                navController.navigate(FacebookScreen.HOME.name)
                delay(100)
                assertTrue(navController.currentBackStackEntry?.destination?.route == FacebookScreen.HOME.name)
                assertTrue(navController.previousBackStackEntry?.destination?.route == FacebookScreen.CREATE_CHAT_GROUP.name)
                val canNavigateUp = navController.navigateUp()
                assertTrue(canNavigateUp)
                assertTrue(navController.currentBackStackEntry?.destination?.route == FacebookScreen.CREATE_CHAT_GROUP.name)
            }
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun friendListIsDisplayed() = runTest {
        composeTestRule.setContent {
            CreateChatGroupScreen(
                navController = navController,
                friendViewModel = friendsViewModel,
                createChatGroupViewModel = createChatGroupViewModel,
                userViewModel = userViewModel
            )
        }

        composeTestRule.waitForIdle()

        friendsViewModel.uiState.value.friends.forEach { friend ->
            if (friend.status == "accepted") {
                val friendUser = userViewModel.getUserById(friend.from).value
                Log.d("FriendListTest", "Found friend: $friendUser")

                friendUser?.let { user ->
                    val fullName = "${user.firstName} ${user.lastName}"
                    Log.d("FriendListTest", "Checking for friend: $fullName")

                    Thread.sleep(1000)
                    composeTestRule.onNodeWithText(fullName).assertIsDisplayed()

                    // Check if at least one "Add" button exists
                    composeTestRule.onAllNodesWithText("Add")
                        .fetchSemanticsNodes()
                        .isNotEmpty()
                        .let { hasAddButton ->
                            assertTrue("No 'Add' button found for $fullName", hasAddButton)
                        }
                }
            }
        }
    }
}