package com.example.facebook.ui.screen

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.facebook.data.ChatGroupRepository
import com.example.facebook.data.FriendRepository
import com.example.facebook.data.UserRepository
import com.example.facebook.model.Friend
import com.example.facebook.model.GetFriendResponse
import com.example.facebook.model.GetFriendSuggestionsResponse
import com.example.facebook.ui.FacebookScreen
import com.example.facebook.ui.screens.CreateChatGroupScreen
import com.example.facebook.ui.screens.CreateChatGroupViewModel
import com.example.facebook.ui.screens.FriendsViewModel
import com.example.facebook.ui.screens.UserViewModel
import io.mockk.mockk
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Response

class FakeFriendRepository : FriendRepository {
    private val mockFriend1 = Friend(
        _id = "1",
        from = "user1",
        to = "user2",
        status = "accepted",
        createdAt = "2024-12-08T19:26:06.898Z",
        updatedAt = "2024-12-08T19:26:06.898Z")

    private val mockFriend2 = Friend(
        _id = "2",
        from = "user2",
        to = "user3",
        status = "pending",
        createdAt = "2024-12-08T19:26:06.898Z",
        updatedAt = "2024-12-08T19:26:06.898Z")

    private val mockFriends = listOf(mockFriend1, mockFriend2)
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
        return Response.success(GetFriendResponse(data = mockFriends, hasMore = false))
    }

    override suspend fun getSuggestions(
        offset: Int?,
        limit: Int?,
        q: String?
    ): Response<GetFriendSuggestionsResponse> {
        TODO("Not yet implemented")
    }
}

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
        // Sử dụng FakeUserRepository thay vì mock UserApiService
        userRepository = fakeUserRepository()
        friendRepository = FakeFriendRepository()
        chatGroupRepository = FakeChatGroup()

        createChatGroupViewModel = CreateChatGroupViewModel(
            chatGroupRepository = chatGroupRepository,
            application = ApplicationProvider.getApplicationContext()
        )

        friendsViewModel = FriendsViewModel(
            friendRepository = friendRepository,
            application = ApplicationProvider.getApplicationContext()
        )

        userViewModel = UserViewModel(
            userRepository = userRepository,
            socketRepository = mockk(relaxed = true),
            userPreferenceRepository = mockk(relaxed = true),
            application = ApplicationProvider.getApplicationContext()
        )

        // Khởi tạo TestNavHostController
        navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        navController.navigatorProvider.addNavigator(ComposeNavigator())
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
                val isFriend = userRepository.getById(friend.to).body()
                isFriend?.let {
                    val fullName = "${it.firstName} ${it.lastName}"
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