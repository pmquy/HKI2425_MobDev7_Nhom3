package com.example.facebook.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.facebook.FacebookApplication
import com.example.facebook.ui.FacebookScreen
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlinx.coroutines.runBlocking


@RunWith(AndroidJUnit4::class)
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var navController: TestNavHostController
    private lateinit var userViewModel: UserViewModel
    private lateinit var homeViewModel: HomeViewModel

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
            chatGroupRepository = application.container.chatGroupRepository
        )

        runBlocking {
            userViewModel.login("hieuma535@gmail.com", "mahieu1010")
        }
    }

    @Test
    fun testHomeScreenDisplaysCorrectly() {
        composeTestRule.setContent {
            HomeScreen(navController = navController, userViewModel = userViewModel, homeViewModel = homeViewModel)
        }
        homeViewModel.uiState.value.chatGroups.firstOrNull()?.name?.let { groupName ->
            composeTestRule.onNodeWithText(groupName).assertExists().assertIsDisplayed()
        }
        composeTestRule.onNodeWithText("Zola").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Search").assertExists().assertIsEnabled().assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Friends").assertExists().assertIsEnabled().assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Settings").assertExists().assertIsEnabled().assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Create Chat Group").assertExists().assertIsEnabled().assertIsDisplayed()
    }

    @Test
    fun testNavigationToSearchScreen() {
        lateinit var navController: NavHostController

        composeTestRule.setContent {
            navController = rememberNavController()
            NavHost(
                navController = navController,
                startDestination = FacebookScreen.HOME.name
            ) {
                composable(FacebookScreen.HOME.name) {
                    HomeScreen(navController = navController, userViewModel = userViewModel, homeViewModel = homeViewModel)
                }
                composable(FacebookScreen.FRIEND_SEARCHING.name) {
                    FindUserScreen(navController = navController)
                }
            }
        }
        composeTestRule.onNodeWithContentDescription("Search").assertExists().assertIsEnabled().assertIsDisplayed().performClick()

        composeTestRule.waitForIdle()

        composeTestRule.runOnUiThread {
            assertEquals(FacebookScreen.FRIEND_SEARCHING.name, navController.currentBackStackEntry?.destination?.route)
        }
    }


    @Test
    fun testNavigationToFriendsScreen() {
        lateinit var navController: NavHostController
        composeTestRule.setContent {
            navController = rememberNavController()
            NavHost(
                navController = navController,
                startDestination = FacebookScreen.HOME.name
            ) {
                composable(FacebookScreen.HOME.name) {
                    HomeScreen(navController = navController, userViewModel = userViewModel, homeViewModel = homeViewModel)
                }
                composable(FacebookScreen.FRIENDS.name) {
                    FriendsScreen(navController = navController, userViewModel = userViewModel)
                }
            }
        }

        composeTestRule.onNodeWithContentDescription("Friends").assertExists().assertIsDisplayed().performClick()

        composeTestRule.waitForIdle()

        composeTestRule.runOnUiThread {
            assertEquals(FacebookScreen.FRIENDS.name, navController.currentBackStackEntry?.destination?.route)
        }
    }

    @Test
    fun testNavigationToCreateChatGroupScreen() {
        lateinit var navController: NavHostController
        composeTestRule.setContent {
            navController = rememberNavController()
            NavHost(
                navController = navController,
                startDestination = FacebookScreen.HOME.name
            ) {
                composable(FacebookScreen.HOME.name) {
                    HomeScreen(navController = navController, userViewModel = userViewModel)
                }
                composable(FacebookScreen.CREATE_CHAT_GROUP.name) {
                    CreateChatGroupScreen(navController = navController)
                }
            }
        }
        composeTestRule.onNodeWithContentDescription("Create Chat Group").assertExists().assertIsDisplayed().performClick()

        composeTestRule.waitForIdle()

        composeTestRule.runOnUiThread {
            assertEquals(FacebookScreen.CREATE_CHAT_GROUP.name, navController.currentBackStackEntry?.destination?.route)
        }
    }

    @Test
    fun testNavigationToProfileScreen() {

        val userId = userViewModel.uiState.value.user._id

        lateinit var navController: NavHostController

        composeTestRule.setContent {
            navController = rememberNavController()
            NavHost(
                navController = navController,
                startDestination = FacebookScreen.HOME.name
            ) {
                composable(FacebookScreen.HOME.name) {
                    HomeScreen(navController = navController, userViewModel = userViewModel)
                }
                composable("${FacebookScreen.PROFILE.name}/${userId}") {
                    ProfileScreen(navController = navController)
                }
            }
        }

        composeTestRule.onNodeWithContentDescription("Settings").assertExists().assertIsEnabled().assertIsDisplayed().performClick()

        composeTestRule.runOnUiThread {
            assertEquals("${FacebookScreen.PROFILE.name}/${userId}", navController.currentBackStackEntry?.destination?.route)
        }
    }

    @Test
    fun testNavigationToChatGroupScreen() {
        lateinit var navController: NavHostController

        val chatGroup = homeViewModel.uiState.value.chatGroups.firstOrNull() ?: return

        val chatGroupId = chatGroup._id

        composeTestRule.setContent {
            navController = rememberNavController()
            NavHost(
                navController = navController,
                startDestination = FacebookScreen.HOME.name
            ) {
                composable(FacebookScreen.HOME.name) {
                    HomeScreen(navController = navController, userViewModel = userViewModel, homeViewModel = homeViewModel)
                }
                composable("${FacebookScreen.CHAT_GROUP.name}/${chatGroupId}") {
                    ChatGroupScreen(navController = navController)
                }
            }
        }
        composeTestRule.waitForIdle()

        // Click vào chat group
        composeTestRule.onNodeWithText(chatGroup.name).performClick()
        composeTestRule.waitUntil(
            timeoutMillis = 5000
        ) {
            composeTestRule.onAllNodesWithText("Send Message").fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText("Send Message").assertIsDisplayed()

        composeTestRule.runOnUiThread {
            assertEquals("${FacebookScreen.CHAT_GROUP.name}/${chatGroupId}", navController.currentBackStackEntry?.destination?.route)
        }
    }
}