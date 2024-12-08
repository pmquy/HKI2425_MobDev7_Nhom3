//package com.example.facebook
//
//
//import androidx.compose.ui.test.assertIsEnabled
//import androidx.compose.ui.test.junit4.createComposeRule
//import androidx.compose.ui.test.onNodeWithContentDescription
//import androidx.compose.ui.test.onNodeWithText
//import androidx.compose.ui.test.performClick
//import androidx.compose.ui.test.performTextInput
//import androidx.navigation.NavHostController
//import androidx.navigation.compose.ComposeNavigator
//import androidx.navigation.compose.NavHost
//import androidx.navigation.compose.composable
//import androidx.navigation.compose.rememberNavController
//import androidx.navigation.testing.TestNavHostController
//import androidx.test.core.app.ApplicationProvider
//import com.example.facebook.data.SocketRepository
//import com.example.facebook.data.UserPreferenceRepository
//import com.example.facebook.data.UserRepository
//import com.example.facebook.model.ChatGroup
//import com.example.facebook.model.User
//import com.example.facebook.ui.FacebookScreen
//import com.example.facebook.ui.screens.CreateChatGroupScreen
//import com.example.facebook.ui.screens.FindUserScreen
//import com.example.facebook.ui.screens.FriendsScreen
//import com.example.facebook.ui.screens.HomeScreen
//import com.example.facebook.ui.screens.HomeUIState
//import com.example.facebook.ui.screens.HomeViewModel
//import com.example.facebook.ui.screens.LoginScreen
//import com.example.facebook.ui.screens.ProfileScreen
//import com.example.facebook.ui.screens.SignUpScreen
//import com.example.facebook.ui.screens.UserViewModel
//import io.mockk.every
//import io.mockk.mockk
//import io.mockk.verify
//import junit.framework.TestCase.assertEquals
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import org.junit.Before
//import org.junit.Rule
//import org.junit.Test
//
//class HomeScreenTest {
//
//    @get:Rule
//    val composeTestRule = createComposeRule()
//    private val navController = TestNavHostController(ApplicationProvider.getApplicationContext()).apply {
//        navigatorProvider.addNavigator(ComposeNavigator())
//    }
//
//    private val mockHomeViewModel = mockk<HomeViewModel>()
//    private val mockUserViewModel = mockk<UserViewModel>()
//    private val mockNavController = mockk<NavHostController>(relaxed = true)
//
//
//    @Test
//    fun testHomeScreenInitialization() {
//        composeTestRule.setContent {
//            HomeScreen(navController = navController, userViewModel = mockUserViewModel)
//        }
//
//        // Ensure the HomeScreen initializes correctly
//        composeTestRule.onNodeWithText("Facebook").assertExists()
//        composeTestRule.onNodeWithContentDescription("Search").assertExists().assertIsEnabled()
//        composeTestRule.onNodeWithContentDescription("Home").assertExists().assertIsEnabled()
//        composeTestRule.onNodeWithContentDescription("Friends").assertExists().assertIsEnabled()
//        composeTestRule.onNodeWithContentDescription("Settings").assertExists().assertIsEnabled()
//        composeTestRule.onNodeWithContentDescription("Menu").assertExists().assertIsEnabled()
//        composeTestRule.onNodeWithContentDescription("Create Chat Group").assertExists().assertIsEnabled()
//    }
//
//    @Test
//    fun testNavigationToSearchScreen() {
//        composeTestRule.setContent {
//            NavHost(
//                navController = navController,
//                startDestination = FacebookScreen.HOME.name
//            ) {
//                composable(FacebookScreen.HOME.name) {
//                    HomeScreen(navController = navController, userViewModel = mockUserViewModel)
//                }
//                composable(FacebookScreen.FRIEND_SEARCHING.name) {
//                    FindUserScreen(navController = navController)
//                }
//            }
//        }
//        // Test Search Icon Navigation
//        composeTestRule.onNodeWithContentDescription("Search").performClick()
//        assertEquals(FacebookScreen.FRIEND_SEARCHING.name, navController.currentDestination?.route)
//    }
//    @Test
//    fun testNavigationToFriendsScreen() {
//        composeTestRule.setContent {
//            NavHost(
//                navController = navController,
//                startDestination = FacebookScreen.HOME.name
//            ) {
//                composable(FacebookScreen.HOME.name) {
//                    HomeScreen(navController = navController, userViewModel = mockUserViewModel)
//                }
//                composable(FacebookScreen.FRIENDS.name) {
//                    FriendsScreen(navController = navController, userViewModel = mockUserViewModel)
//                }
//            }
//        }
//        // Test Search Icon Navigation
//        composeTestRule.onNodeWithContentDescription("Friends").performClick()
//        assertEquals(FacebookScreen.FRIENDS.name, navController.currentDestination?.route)
//    }
//
//    @Test
//    fun testNavigationToCreateChatGroupScreen() {
//        composeTestRule.setContent {
//            NavHost(
//                navController = navController,
//                startDestination = FacebookScreen.HOME.name
//            ) {
//                composable(FacebookScreen.HOME.name) {
//                    HomeScreen(navController = navController, userViewModel = mockUserViewModel)
//                }
//                composable(FacebookScreen.CREATE_CHAT_GROUP.name) {
//                    CreateChatGroupScreen(navController = navController)
//                }
//            }
//        }
//        // Test Search Icon Navigation
//        composeTestRule.onNodeWithContentDescription("Create Chat Group").performClick()
//        assertEquals(FacebookScreen.CREATE_CHAT_GROUP.name, navController.currentDestination?.route)
//    }
//
//    @Test
//    fun testNavigationToProfileScreen() {
//        composeTestRule.setContent {
//            NavHost(
//                navController = navController,
//                startDestination = FacebookScreen.HOME.name
//            ) {
//                composable(FacebookScreen.HOME.name) {
//                    HomeScreen(navController = navController, userViewModel = mockUserViewModel)
//                }
//                composable(FacebookScreen.PROFILE.name) {
//                    ProfileScreen(navController = navController)
//                }
//            }
//        }
//        // Test Search Icon Navigation
//        composeTestRule.onNodeWithContentDescription("Settings").performClick()
//        assertEquals(FacebookScreen.PROFILE.name, navController.currentDestination?.route)
//    }
//
//
//
//    @Test
//    fun testFloatingActionButtonNavigation() {
//        val uiState = MutableStateFlow(HomeUIState())
//        every { mockHomeViewModel.uiState } returns uiState
//
//        composeTestRule.setContent {
//            HomeScreen(
//                homeViewModel = mockHomeViewModel,
//                userViewModel = mockUserViewModel,
//                navController = mockNavController
//            )
//        }
//
//        // Test FloatingActionButton Navigation
//        composeTestRule.onNodeWithText("Create Chat Group").performClick()
//        verify { mockNavController.navigate(FacebookScreen.CREATE_CHAT_GROUP.name) }
//    }
//
//    @Test
//    fun testChatGroupRendering() {
//        val chatGroups = listOf(
//            ChatGroup(
//                _id = "1",
//                name = "Group 1",
//                avatar = "avatar1",
//                lastMessage = null
//            )
//        )
//        val uiState = MutableStateFlow(HomeUIState(chatGroups = chatGroups))
//        every { mockHomeViewModel.uiState } returns uiState
//
//        composeTestRule.setContent {
//            HomeScreen(
//                homeViewModel = mockHomeViewModel,
//                userViewModel = mockUserViewModel,
//                navController = mockNavController
//            )
//        }
//
//        // Ensure ChatGroup is rendered correctly
//        composeTestRule.onNodeWithText("Group 1").assertExists()
//    }
//}
package com.example.facebook

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.NavHostController
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.example.facebook.data.SocketRepository
import com.example.facebook.data.UserPreferenceRepository
import com.example.facebook.data.UserRepository
import com.example.facebook.model.ChatGroup
import com.example.facebook.model.User
import com.example.facebook.ui.FacebookScreen
import com.example.facebook.ui.screens.CreateChatGroupScreen
import com.example.facebook.ui.screens.FindUserScreen
import com.example.facebook.ui.screens.FriendsScreen
import com.example.facebook.ui.screens.HomeScreen
import com.example.facebook.ui.screens.HomeUIState
import com.example.facebook.ui.screens.HomeViewModel
import com.example.facebook.ui.screens.LoginScreen
import com.example.facebook.ui.screens.ProfileScreen
import com.example.facebook.ui.screens.SignUpScreen
import com.example.facebook.ui.screens.UIState
import com.example.facebook.ui.screens.UserViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Response

class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val navController = TestNavHostController(ApplicationProvider.getApplicationContext()).apply {
        navigatorProvider.addNavigator(ComposeNavigator())
    }

    // Mock User
    private val mockUser = User(
        _id = "123",
        firstName = "John",
        lastName = "Doe",
        email = "john.doe@example.com",
        phoneNumber = "019234156",
        avatar = "avatar_url",
        password = "password",
        createdAt = "2024-12-08T19:26:06.898Z",
        updatedAt = "2024-12-08T19:26:06.898Z"
    )

    // Mock SocketRepository
    private val mockSocketRepository = mockk<SocketRepository> {
        every { getID() } returns "mockSocketID"
    }

    // Mock UserRepository
    private val mockUserRepository = mockk<UserRepository> {
        coEvery { getById("123") } returns Response.success(mockUser)
        coEvery { login(any(), any(), any(), any()) } returns Response.success(mockUser)
        coEvery { update(any(), any(), any(), any(), any()) } returns Response.success(mockUser)
        coEvery { auth(any(), any()) } returns Response.success(mockUser)
    }

    // Mock UserPreferenceRepository
    private val mockPreferenceRepository = mockk<UserPreferenceRepository> {
        every { getToken() } returns "mockToken"
    }

    // Mock HomeViewModel
    private val mockHomeViewModel = mockk<HomeViewModel>(relaxed = true)

    // Mock UserViewModel
    private val mockUserViewModel = mockk<UserViewModel> {
        every { uiState.value } returns UIState(user = mockUser)
        every { checkIfUser("123") } returns true
        every { getUserById("123") } returns MutableStateFlow(mockUser)
    }

    @Test
    fun testHomeScreenInitialization() {
        composeTestRule.setContent {
            HomeScreen(navController = navController, userViewModel = mockUserViewModel)
        }

        // Ensure the HomeScreen initializes correctly
        composeTestRule.onNodeWithText("Facebook").assertExists()
        composeTestRule.onNodeWithContentDescription("Search").assertExists().assertIsEnabled()
        composeTestRule.onNodeWithContentDescription("Home").assertExists().assertIsEnabled()
        composeTestRule.onNodeWithContentDescription("Friends").assertExists().assertIsEnabled()
        composeTestRule.onNodeWithContentDescription("Settings").assertExists().assertIsEnabled()
        composeTestRule.onNodeWithContentDescription("Menu").assertExists().assertIsEnabled()
        composeTestRule.onNodeWithContentDescription("Create Chat Group").assertExists().assertIsEnabled()
    }

    @Test
    fun testNavigationToSearchScreen() {
        composeTestRule.setContent {
            NavHost(
                navController = navController,
                startDestination = FacebookScreen.HOME.name
            ) {
                composable(FacebookScreen.HOME.name) {
                    HomeScreen(navController = navController, userViewModel = mockUserViewModel)
                }
                composable(FacebookScreen.FRIEND_SEARCHING.name) {
                    FindUserScreen(navController = navController)
                }
            }
        }
        // Test Search Icon Navigation
        composeTestRule.onNodeWithContentDescription("Search").performClick()
        assertEquals(FacebookScreen.FRIEND_SEARCHING.name, navController.currentDestination?.route)
    }

    @Test
    fun testNavigationToFriendsScreen() {
        composeTestRule.setContent {
            NavHost(
                navController = navController,
                startDestination = FacebookScreen.HOME.name
            ) {
                composable(FacebookScreen.HOME.name) {
                    HomeScreen(navController = navController, userViewModel = mockUserViewModel)
                }
                composable(FacebookScreen.FRIENDS.name) {
                    FriendsScreen(navController = navController, userViewModel = mockUserViewModel)
                }
            }
        }
        // Test Friends Icon Navigation
        composeTestRule.onNodeWithContentDescription("Friends").performClick()
        assertEquals(FacebookScreen.FRIENDS.name, navController.currentDestination?.route)
    }

    @Test
    fun testNavigationToCreateChatGroupScreen() {
        composeTestRule.setContent {
            NavHost(
                navController = navController,
                startDestination = FacebookScreen.HOME.name
            ) {
                composable(FacebookScreen.HOME.name) {
                    HomeScreen(navController = navController, userViewModel = mockUserViewModel)
                }
                composable(FacebookScreen.CREATE_CHAT_GROUP.name) {
                    CreateChatGroupScreen(navController = navController)
                }
            }
        }
        // Test Create Chat Group Icon Navigation
        composeTestRule.onNodeWithContentDescription("Create Chat Group").performClick()
        assertEquals(FacebookScreen.CREATE_CHAT_GROUP.name, navController.currentDestination?.route)
    }

    @Test
    fun testNavigationToProfileScreen() {
        composeTestRule.setContent {
            NavHost(
                navController = navController,
                startDestination = FacebookScreen.HOME.name
            ) {
                composable(FacebookScreen.HOME.name) {
                    HomeScreen(navController = navController, userViewModel = mockUserViewModel)
                }
                composable(FacebookScreen.PROFILE.name) {
                    ProfileScreen(navController = navController)
                }
            }
        }
        // Test Settings Icon Navigation
        composeTestRule.onNodeWithContentDescription("Settings").performClick()
        assertEquals(FacebookScreen.PROFILE.name, navController.currentDestination?.route)
    }

    @Test
    fun testFloatingActionButtonNavigation() {
        composeTestRule.setContent {
            HomeScreen(
                homeViewModel = mockHomeViewModel,
                userViewModel = mockUserViewModel,
                navController = navController
            )
        }

        // Test FloatingActionButton Navigation
        composeTestRule.onNodeWithContentDescription("Create Chat Group").performClick()
        verify { navController.navigate(FacebookScreen.CREATE_CHAT_GROUP.name) }
    }

    @Test
    fun testChatGroupRendering() {
        val chatGroups = listOf(
            ChatGroup(
                _id = "1",
                name = "Group 1",
                avatar = "avatar1",
                lastMessage = null
            )
        )
        val uiState = MutableStateFlow(HomeUIState(chatGroups = chatGroups))
        every { mockHomeViewModel.uiState } returns uiState

        composeTestRule.setContent {
            HomeScreen(
                homeViewModel = mockHomeViewModel,
                userViewModel = mockUserViewModel,
                navController = navController
            )
        }

        // Ensure ChatGroup is rendered correctly
        composeTestRule.onNodeWithText("Group 1").assertExists()
    }
}
