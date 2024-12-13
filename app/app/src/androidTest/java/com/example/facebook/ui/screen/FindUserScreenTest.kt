package com.example.facebook.ui.screen

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.facebook.ui.FacebookScreen
import com.example.facebook.ui.screens.FindUserScreen
import com.example.facebook.ui.screens.FindUserViewModel
import com.example.facebook.ui.screens.ResultList
import com.example.facebook.ui.screens.UserViewModel
import io.mockk.mockk
import kotlinx.coroutines.delay
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FindUserScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var navController: TestNavHostController
    private lateinit var userViewModel: UserViewModel
    private lateinit var findUserViewModel: FindUserViewModel

    @Before
    fun setUp() {
        userViewModel = UserViewModel(
            userRepository = fakeUserRepository(),
            socketRepository = mockk(relaxed = true),
            userPreferenceRepository = mockk(relaxed = true),
            application = ApplicationProvider.getApplicationContext()
        )

        findUserViewModel = FindUserViewModel(
            userRepository = fakeUserRepository(),
            application = ApplicationProvider.getApplicationContext()
        )

        navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        navController.navigatorProvider.addNavigator(ComposeNavigator())
    }

    @Test
    fun testFindUserScreenDisplaysCorrectly() {
        composeTestRule.setContent {
            FindUserScreen(
                navController = navController,
                findUserViewModel = findUserViewModel
            )
        }
        composeTestRule.onNodeWithText("Tìm kiếm").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Back").assertExists().assertIsDisplayed()
    }

    @Test
    fun testNavigationToBackScreen() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            NavHost(
                navController = navController,
                startDestination = FacebookScreen.FRIEND_SEARCHING.name
            ) {
                composable(FacebookScreen.FRIEND_SEARCHING.name) {
                    FindUserScreen(
                        navController = navController,
                        findUserViewModel = findUserViewModel
                    )
                }
                composable(FacebookScreen.PROFILE.name) {
                }
            }

            LaunchedEffect(Unit) {
                navController.navigate(FacebookScreen.PROFILE.name)
                delay(100)
                assertTrue(navController.currentBackStackEntry?.destination?.route == FacebookScreen.PROFILE.name)
                assertTrue(navController.previousBackStackEntry?.destination?.route == FacebookScreen.FRIEND_SEARCHING.name)
                val canNavigateUp = navController.navigateUp()
                assertTrue(canNavigateUp)
                assertTrue(navController.currentBackStackEntry?.destination?.route == FacebookScreen.FRIEND_SEARCHING.name)
            }
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun testResultList() {
        composeTestRule.setContent {
            ResultList(
                navController = navController,
                users = fakeUserRepository().getAllUsers(),
            )
        }
        fakeUserRepository().getAllUsers().forEach { user ->
            composeTestRule.onNodeWithText("${user.firstName} ${user.lastName}").assertExists().assertIsDisplayed()
        }
        composeTestRule.waitForIdle()
    }
}