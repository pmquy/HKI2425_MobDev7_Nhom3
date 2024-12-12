package com.example.facebook.ui.screen

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
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
import com.example.facebook.ui.screens.VideoCallScreen
import com.example.facebook.ui.screens.VideoCallViewModel
import io.mockk.mockk
import kotlinx.coroutines.delay
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VideoCallScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var videoCallViewModel: VideoCallViewModel
    private lateinit var navController: TestNavHostController
    @Before
    fun setUp() {
        videoCallViewModel = VideoCallViewModel(
            application = ApplicationProvider.getApplicationContext(),
            userRepository = fakeUserRepository(),
            socketRepository = mockk(relaxed = true)
        )
        navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        navController.navigatorProvider.addNavigator(ComposeNavigator())
    }

    @Test
    fun testVideoCallScreen() {
        composeTestRule.setContent {
            VideoCallScreen(
                navController = navController,
                videoCallViewModel = videoCallViewModel
            )
        }
        composeTestRule.onNodeWithText("VIDEO CALL").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Back").assertIsDisplayed().assertIsEnabled()
        composeTestRule.onNodeWithContentDescription("Mic").assertIsDisplayed().assertIsEnabled()
        composeTestRule.onNodeWithContentDescription("Camera").assertIsDisplayed().assertIsEnabled()
        composeTestRule.onNodeWithContentDescription("Flip Camera").assertIsDisplayed().assertIsEnabled()
        composeTestRule.onNodeWithContentDescription("Phone off").assertIsDisplayed().assertIsEnabled()
    }

    @Test
    fun testNavigationToBackScreen() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            NavHost(
                navController = navController,
                startDestination = FacebookScreen.VIDEO_CALL.name
            ) {
                composable(FacebookScreen.VIDEO_CALL.name) {
                    VideoCallScreen(
                        navController = navController,
                    )
                }
                composable(FacebookScreen.CHAT_GROUP.name) {
                }
            }

            LaunchedEffect(Unit) {
                navController.navigate(FacebookScreen.CHAT_GROUP.name)
                delay(100)
                assertTrue(navController.currentBackStackEntry?.destination?.route == FacebookScreen.CHAT_GROUP.name)
                assertTrue(navController.previousBackStackEntry?.destination?.route == FacebookScreen.VIDEO_CALL.name)
                val canNavigateUp = navController.navigateUp()
                assertTrue(canNavigateUp)
                assertTrue(navController.currentBackStackEntry?.destination?.route == FacebookScreen.VIDEO_CALL.name)
            }
        }
        composeTestRule.waitForIdle()
    }
}