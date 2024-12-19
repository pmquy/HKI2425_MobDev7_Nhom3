package com.example.facebook.ui.screens


import androidx.compose.runtime.LaunchedEffect
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
import com.example.facebook.ui.FacebookScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FriendsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var navController: TestNavHostController
    private lateinit var userViewModel: UserViewModel
    private lateinit var friendsViewModel: FriendsViewModel

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

        friendsViewModel = FriendsViewModel(
            friendRepository = application.container.friendRepository,
            application = application
        )

        runBlocking {
            userViewModel.login("hieuma535@gmail.com", "mahieu1010")
        }
    }

    @Test
    fun testFriendsScreenDisplaysCorrectly() {
        composeTestRule.setContent {
            FriendsScreen(
                userViewModel = userViewModel,
                friendsViewModel = friendsViewModel,
                navController = navController
            )
        }
        composeTestRule.onNodeWithText("Friends").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Search").assertExists().assertIsEnabled().assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Back").assertExists().assertIsDisplayed().assertIsEnabled()
        composeTestRule.onNodeWithTag("FriendSubScreen").assertExists().assertIsDisplayed()
    }


    @Test
    fun testFriendSubScreenDisplaysCorrectly() {
        composeTestRule.setContent {
            FriendsScreen(
                userViewModel = userViewModel,
                friendsViewModel = friendsViewModel,
                navController = navController
            )
        }

        FriendSubScreen.entries.forEach { subScreen ->
            composeTestRule.onNodeWithText(subScreen.tag).assertExists().assertIsDisplayed()
        }
    }
    @Test
    fun testNavigationToBackScreen() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            NavHost(
                navController = navController,
                startDestination = FacebookScreen.FRIENDS.name
            ) {
                composable(FacebookScreen.FRIENDS.name) {
                    FriendsScreen(
                        userViewModel = userViewModel,
                        friendsViewModel = friendsViewModel,
                        navController = navController
                    )
                }
                composable(FacebookScreen.PROFILE.name) {
                }
            }
            LaunchedEffect(Unit) {
                navController.navigate(FacebookScreen.PROFILE.name)
                delay(100)
                assertTrue(navController.currentBackStackEntry?.destination?.route == FacebookScreen.PROFILE.name)
                assertTrue(navController.previousBackStackEntry?.destination?.route == FacebookScreen.FRIENDS.name)
                val canNavigateUp = navController.navigateUp()
                assertTrue(canNavigateUp)
                assertTrue(navController.currentBackStackEntry?.destination?.route == FacebookScreen.FRIENDS.name)
            }
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun testSearchFriend() {
        composeTestRule.setContent {
            FriendsScreen(
                userViewModel = userViewModel,
                friendsViewModel = friendsViewModel,
                navController = navController
            )
        }
        composeTestRule.onNodeWithContentDescription("Search").performClick()
        composeTestRule.onNodeWithText("Tìm kiếm").assertExists().assertIsDisplayed()
        composeTestRule.waitForIdle()
    }

//    @Test
//    fun testFriendSubScreenClickCorrectly() {
//        composeTestRule.setContent {
//            FriendsScreen(
//                userViewModel = userViewModel,
//                friendsViewModel = friendsViewModel,
//                navController = navController
//            )
//        }
//
//        FriendSubScreen.entries.forEach { subScreen ->
//            composeTestRule.onNodeWithText(subScreen.tag).performClick()
//            composeTestRule.waitForIdle()
//
//            when (subScreen) {
//                FriendSubScreen.SUGGESTS -> {
//                    composeTestRule.onNodeWithTag("Suggestions").assertExists()
//                }
//                FriendSubScreen.REQUESTS -> {
//                    composeTestRule.onNodeWithTag("Requests").assertExists()
//                }
//                FriendSubScreen.ALL -> {
//                    composeTestRule.onNodeWithTag("AllFriends").assertExists()
//                }
//                FriendSubScreen.SENTS -> {
//                    composeTestRule.onNodeWithTag("Sents").assertExists()
//                }
//            }
//        }
//    }
}