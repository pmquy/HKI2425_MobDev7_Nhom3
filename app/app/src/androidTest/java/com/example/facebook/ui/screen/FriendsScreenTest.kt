package com.example.facebook.ui.screen

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.facebook.data.FriendRepository
import com.example.facebook.model.Friend
import com.example.facebook.model.GetFriendResponse
import com.example.facebook.model.GetFriendSuggestionsResponse
import com.example.facebook.ui.FacebookScreen
import com.example.facebook.ui.screens.FriendSubScreen
import com.example.facebook.ui.screens.FriendsScreen
import com.example.facebook.ui.screens.FriendsViewModel
import com.example.facebook.ui.screens.UserViewModel
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.delay
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Response

class fakeFriendsRepository : FriendRepository {
    val mockFrined1 = Friend(
        _id = "1",
        from = "user1",
        to = "user2",
        status = "accepted",
        createdAt = "2024-12-08T19:26:06.898Z",
        updatedAt = "2024-12-08T19:26:06.898Z"
    )

    val mockFriend2 = Friend(
        _id = "2",
        from = "user2",
        to = "user3",
        status = "pending",
        createdAt = "2024-12-08T19:26:06.898Z",
        updatedAt = "2024-12-08T19:26:06.898Z"
    )

    override suspend fun request(to: String) {
        TODO("Not yet implemented")
    }

    override suspend fun accept(from: String) {
        TODO("Not yet implemented")
    }

    override suspend fun decline(from: String) {
        TODO("Not yet implemented")
    }

    override suspend fun revoke(to: String) {
        TODO("Not yet implemented")
    }

    override suspend fun disfriend(from: String) {
        TODO("Not yet implemented")
    }

    override suspend fun getAll(
        offset: Int?,
        limit: Int?,
        q: String?
    ): Response<GetFriendResponse> {
        return Response.success(GetFriendResponse(data = listOf(mockFrined1, mockFriend2), hasMore = false))
    }

    override suspend fun getSuggestions(
        offset: Int?,
        limit: Int?,
        q: String?
    ): Response<GetFriendSuggestionsResponse> {
        return Response.success(GetFriendSuggestionsResponse(data = listOf("user4", "user5"), hasMore = false))
    }
}
@RunWith(AndroidJUnit4::class)
class FriendsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var navController: TestNavHostController
    private lateinit var userViewModel: UserViewModel
    private lateinit var friendsViewModel: FriendsViewModel

    @Before
    fun setUp() {
        userViewModel = UserViewModel(
            userRepository = fakeUserRepository(),
            socketRepository = mockk(relaxed = true),
            userPreferenceRepository = mockk(relaxed = true),
            application = ApplicationProvider.getApplicationContext()
        )

        friendsViewModel = FriendsViewModel(
            friendRepository = fakeFriendsRepository(),
            application = ApplicationProvider.getApplicationContext()
        )

        navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        navController.navigatorProvider.addNavigator(ComposeNavigator())
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

        // Verify that each sub-screen is displayed correctly
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

    @Test
    fun testFriendSubScreenClickCorrectly() {
        composeTestRule.setContent {
            FriendsScreen(
                userViewModel = userViewModel,
                friendsViewModel = friendsViewModel,
                navController = navController
            )
        }
    
        // Verify that each sub-screen is displayed correctly
        FriendSubScreen.entries.forEach { subScreen ->
            composeTestRule.onNodeWithText(subScreen.tag).performClick()
            composeTestRule.waitForIdle() // Wait for the UI to update
    
            when (subScreen) {
                FriendSubScreen.SUGGESTS -> {
                    composeTestRule.onNodeWithTag("Suggestions").assertExists().assertIsDisplayed()
                }
                FriendSubScreen.REQUESTS -> {
                    composeTestRule.onNodeWithTag("Requests").assertExists().assertIsDisplayed()
                }
                FriendSubScreen.ALL -> {
                    composeTestRule.onNodeWithTag("AllFriends").assertExists().assertIsDisplayed()
                }
                FriendSubScreen.SENTS -> {
                    composeTestRule.onNodeWithTag("Sents").assertExists().assertIsDisplayed()
                }
            }
        }
    }
}