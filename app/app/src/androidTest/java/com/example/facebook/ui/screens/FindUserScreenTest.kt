package com.example.facebook.ui.screens

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
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
class FindUserScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var navController: TestNavHostController
    private lateinit var userViewModel: UserViewModel
    private lateinit var findUserViewModel: FindUserViewModel

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


        findUserViewModel = FindUserViewModel(
            userRepository = application.container.userRepository,
            application = application
        )
        runBlocking {
            userViewModel.login("hieuma535@gmail.com", "mahieu1010")

        }
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
        findUserViewModel.setSearch("mah i")
        composeTestRule.setContent {
            ResultList(
                navController = navController,
                users = findUserViewModel.uiState.value.users ?: emptyList(),
            )
        }
        findUserViewModel.uiState.value.users.forEach { user ->
            composeTestRule.onNodeWithText("${user.firstName} ${user.lastName}").assertExists().assertIsDisplayed()
        }
        composeTestRule.waitForIdle()
    }
}