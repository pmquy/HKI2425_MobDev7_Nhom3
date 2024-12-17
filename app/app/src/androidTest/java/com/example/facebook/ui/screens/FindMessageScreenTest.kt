package com.example.facebook.ui.screens

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.facebook.FacebookApplication
import com.example.facebook.ui.FacebookScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FindMessageScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var navController: TestNavHostController
    private lateinit var userViewModel: UserViewModel
    private lateinit var findMessageViewModel: FindMessageViewModel
    private lateinit var homeViewModel: HomeViewModel

    @Before
    fun setUp() {
        navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        val application = ApplicationProvider.getApplicationContext<FacebookApplication>()

        userViewModel = UserViewModel(
            userRepository = application.container.userRepository,
            socketRepository = application.container.socketRepository,
            userPreferenceRepository = application.container.userPreferenceRepository,
            application = application
        )

        findMessageViewModel = FindMessageViewModel(
            chatGroupRepository = application.container.chatGroupRepository,
            application = application
        )

        homeViewModel = HomeViewModel(
            chatGroupRepository = application.container.chatGroupRepository
        )

        runBlocking {
            userViewModel.login("hieuma535@gmail.com", "mahieu1010")
            findMessageViewModel.setChatGroupId("675b0c77331e1496a99df02d")
        }

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
        findMessageViewModel.setSearch("Hello, group!")
        composeTestRule.onNodeWithText("${findMessageViewModel.uiState.value.offset} tin nhắn khớp").assertExists().assertIsDisplayed()
        composeTestRule.waitForIdle()
    }

//    @Test
//    fun messageCardTest() {
//        findMessageViewModel.setSearch("Hello, group!")
//        findMessageViewModel.findMessages()
//        Log.d("hagse", "MessageCardTest: ${findMessageViewModel.uiState.value.messages} ")
//        composeTestRule.setContent {
//            MessageCard(
//                navController = navController,
//                userViewModel = userViewModel,
//                message = findMessageViewModel.uiState.value.messages.firstOrNull()!!,
//            )
//        }
//        val iduserId = findMessageViewModel.uiState.value.messages.firstOrNull()?.user?: ""
//        val user = "${userViewModel.getUserById(iduserId).value?.lastName} ${userViewModel.getUserById(iduserId).value?.firstName}"
//        composeTestRule.onNodeWithText(user).assertExists().assertIsDisplayed()
//        findMessageViewModel.uiState.value.messages.firstOrNull()
//            ?.let { composeTestRule.onNodeWithText(it.message).assertExists().assertIsDisplayed() }
//
//        composeTestRule.waitForIdle()
//    }
}