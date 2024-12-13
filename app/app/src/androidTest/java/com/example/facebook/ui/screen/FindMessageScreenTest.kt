package com.example.facebook.ui.screen

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.facebook.ui.FacebookScreen
import com.example.facebook.ui.screens.FindMessageScreen
import com.example.facebook.ui.screens.FindMessageViewModel
import com.example.facebook.ui.screens.UserViewModel
import io.mockk.mockk
import kotlinx.coroutines.delay
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import android.util.Log
import com.example.facebook.model.Message
import com.example.facebook.ui.screens.MessageCard
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FindMessageScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var navController: TestNavHostController
    private lateinit var userViewModel: UserViewModel
    private lateinit var findMessageViewModel: FindMessageViewModel

    @Before
    fun setUp() {
        userViewModel = UserViewModel(
            userRepository = fakeUserRepository(),
            socketRepository = mockk(relaxed = true),
            userPreferenceRepository = mockk(relaxed = true),
            application = ApplicationProvider.getApplicationContext()
        )

        findMessageViewModel = FindMessageViewModel(
            chatGroupRepository = FakeChatGroup(),
            application = ApplicationProvider.getApplicationContext()
        )

        navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        navController.navigatorProvider.addNavigator(ComposeNavigator())

    }

    @Test
    fun testFindMessageScreenDisplaysCorrectly() {
        composeTestRule.setContent {
            FindMessageScreen(
                navController = navController,
                findMessageViewModel = findMessageViewModel,
                userViewModel = userViewModel
            )
        }
        composeTestRule.onNodeWithText("Tìm kiếm tin nhắn").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Back").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("${findMessageViewModel.uiState.value.messages.size.toString()} tin nhắn khớp").assertExists().assertIsDisplayed()
    }

    @Test
    fun testNavigationToBackScreen() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            NavHost(
                navController = navController,
                startDestination = FacebookScreen.FIND_MESSAGE.name
            ) {
                composable(FacebookScreen.FIND_MESSAGE.name) {
                    FindMessageScreen(
                        navController = navController,
                        findMessageViewModel = findMessageViewModel,
                        userViewModel = userViewModel
                    )
                }
                composable(FacebookScreen.CHAT_GROUP.name) {
                }
            }

            LaunchedEffect(Unit) {
                navController.navigate(FacebookScreen.CHAT_GROUP.name)
                delay(100)
                assertTrue(navController.currentBackStackEntry?.destination?.route == FacebookScreen.CHAT_GROUP.name)
                assertTrue(navController.previousBackStackEntry?.destination?.route == FacebookScreen.FIND_MESSAGE.name)
                val canNavigateUp = navController.navigateUp()
                assertTrue(canNavigateUp)
                assertTrue(navController.currentBackStackEntry?.destination?.route == FacebookScreen.FIND_MESSAGE.name)
            }
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun testSearchMessage() {
        composeTestRule.setContent {
            FindMessageScreen(
                navController = navController,
                findMessageViewModel = findMessageViewModel,
                userViewModel = userViewModel
            )
        }
        composeTestRule.onNodeWithText("Tìm kiếm tin nhắn").performTextInput("Hello, group!")
        composeTestRule.onNodeWithText("${findMessageViewModel.uiState.value.messages.size.toString()} tin nhắn khớp").assertExists().assertIsDisplayed()
        composeTestRule.waitForIdle()
        //Log.d("FindMessageScreenTest", "testSearchMessage: ${findMessageViewModel.uiState.value.chatGroupId}")
    }

    @Test
    fun MessageCardTest() {
    
        composeTestRule.setContent {
            MessageCard(
                navController = navController,
                userViewModel = userViewModel,
                message = Message(
                    _id = "message1",
                    message = "Hello, world!",
                    chatgroup = "group1",
                    user = "user1",
                    createdAt = "2024-12-08T19:30:00.000Z",
                    updatedAt = "2024-12-08T19:30:00.000Z"
                )
            )
        }
        //Log.d("hagse", "MessageCardTest: ${userViewModel.uiState.value.user.firstName} ${userViewModel.uiState.value.user.lastName}")
        Thread.sleep(1000)
        // Check if the user's name is displayed
        composeTestRule.onNodeWithText("test2 mhias").assertExists().assertIsDisplayed()
    
        // Check if the message content is displayed
        composeTestRule.onNodeWithText("Hello, world!").assertExists().assertIsDisplayed()
    
        composeTestRule.waitForIdle()
    }
}
