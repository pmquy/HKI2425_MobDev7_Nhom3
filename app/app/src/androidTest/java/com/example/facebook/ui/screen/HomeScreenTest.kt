package com.example.facebook.ui.screen

import android.util.Log
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.facebook.data.UserRepository
import com.example.facebook.ui.FacebookScreen
import com.example.facebook.ui.screens.*
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.example.facebook.model.GetUsersResponse
import com.example.facebook.model.User
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
import retrofit2.Response
import java.io.File


class fakeUserRepository : UserRepository {
    private val mockedUser = User(
        _id = "user1",
        firstName = "test2",
        lastName = "mhias",
        email = "ssbkss1010@gmail.com",
        phoneNumber = "0937263813",
        avatar = "6755f2b80d0a71201c9ee387",
        password = "mahieu1010",
        createdAt = "2024-12-08T19:26:06.898Z",
        updatedAt = "2024-12-08T19:26:06.898Z"
    )

    private val mockedUser2 = User(
        _id = "user2",
        firstName = "test3",
        lastName = "mahias",
        email = "adefasf14@gmail.com",
        phoneNumber = "0937263814",
        avatar = "672f78cd4763def725d6974f",
        password = "test3010",
        createdAt = "2024-12-09T19:26:06.898Z",
        updatedAt = "2024-12-24T19:26:06.898Z"
    )
    private val mockedUser3 = User(
        _id = "user3",
        firstName = "test4",
        lastName = "hiua",
        email = "adefasf134455@gmail.com",
        phoneNumber = "0937263815",
        avatar = "672f78cd4763def725d69750",
        password = "test4010",
        createdAt = "2024-12-19T19:26:06.898Z",
        updatedAt = "2024-12-24T19:26:06.898Z"
    )

    private val mockedUsers = listOf(mockedUser, mockedUser2, mockedUser3)

    override suspend fun login(
        email: String,
        password: String,
        token: String?,
        socketId: String?
    ): Response<User> {
        if(email == "ssbkss1010@gmail.com" && password == "mahieu1010") {
            return Response.success(mockedUser)
        } else {
            val errorResponseBody = ResponseBody.create(null, "Invalid credentials")
            return Response.error(500, errorResponseBody)
        }
    }

    override suspend fun auth(token: String?, socketId: String?): Response<User> {
        TODO("Not yet implemented")
    }

    override suspend fun getById(id: String): Response<User> {
        when (id) {
            "user1" -> return Response.success(mockedUser)
            "user2" -> return Response.success(mockedUser2)
            "user3" -> return Response.success(mockedUser3)
            else -> return Response.error(404, ResponseBody.create(null, "User not found"))
        }
    }

    override suspend fun register(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        phoneNumber: String,
        avatar: Pair<File, String>?
    ): Response<User> {
        TODO("Not yet implemented")
    }

    override suspend fun verifyOtp(email: String, otp: String): Response<User> {
        TODO("Not yet implemented")
    }

    override suspend fun update(
        firstName: String?,
        lastName: String?,
        password: String?,
        phoneNumber: String?,
        avatar: Pair<File, String>?
    ): Response<User> {
        TODO("Not yet implemented")
    }

    override suspend fun logout(): Response<Void> {
        TODO("Not yet implemented")
    }

    override suspend fun getUsers(offset: Int, limit: Int, q: String): Response<GetUsersResponse> {
        TODO("Not yet implemented")
    }
}

@RunWith(AndroidJUnit4::class)
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var navController: TestNavHostController
    private lateinit var userRepository: UserRepository
    private lateinit var userViewModel: UserViewModel
    private lateinit var homeViewModel: HomeViewModel

    @Before
    fun setUp() {
        // Sử dụng FakeUserRepository thay vì mock UserApiService
        userRepository = fakeUserRepository()
        homeViewModel = HomeViewModel(
            chatGroupRepository = FakeChatGroup()
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
    fun testHomeScreenDisplaysCorrectly() {
        composeTestRule.setContent {
            HomeScreen(navController = navController, userViewModel = userViewModel, homeViewModel = homeViewModel)
        }
        homeViewModel.uiState.value.chatGroups.firstOrNull()?.name?.let { groupName ->
            composeTestRule.onNodeWithText(groupName).assertExists()
        }

        composeTestRule.onNodeWithText("Facebook").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Search").assertExists().assertIsEnabled().assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Home").assertExists().assertIsEnabled().assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Friends").assertExists().assertIsEnabled().assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Settings").assertExists().assertIsEnabled().assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Menu").assertExists().assertIsEnabled().assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Create Chat Group").assertExists().assertIsEnabled().assertIsDisplayed()
    }

    @Test
    fun testNavigationToSearchScreen() {
        composeTestRule.setContent {
            NavHost(
                navController = navController,
                startDestination = FacebookScreen.HOME.name
            ) {
                composable(FacebookScreen.HOME.name) {
                    HomeScreen(navController = navController, userViewModel = userViewModel)
                }
                composable(FacebookScreen.FRIEND_SEARCHING.name) {
                    FindUserScreen(navController = navController)
                }
            }
        }
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
                    HomeScreen(navController = navController, userViewModel = userViewModel, homeViewModel = homeViewModel)
                }
                composable(FacebookScreen.FRIENDS.name) {
                    FriendsScreen(navController = navController, userViewModel = userViewModel)
                }
            }
        }
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
                    HomeScreen(navController = navController, userViewModel = userViewModel)
                }
                composable(FacebookScreen.CREATE_CHAT_GROUP.name) {
                    CreateChatGroupScreen(navController = navController)
                }
            }
        }
        composeTestRule.onNodeWithContentDescription("Create Chat Group").performClick()
        assertEquals(FacebookScreen.CREATE_CHAT_GROUP.name, navController.currentDestination?.route)
    }

    @Test
    fun testNavigationToProfileScreen() {
        val userId = userViewModel.uiState.value.user._id
        composeTestRule.setContent {
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
        composeTestRule.onNodeWithContentDescription("Settings").performClick()
        assertEquals("${FacebookScreen.PROFILE.name}/${userId}", navController.currentDestination?.route)
    }

    @Test
    fun testNavigationToChatGroupScreen() {
        // Ensure there's at least one chat group
        val chatGroup = homeViewModel.uiState.value.chatGroups.firstOrNull()
        if (chatGroup == null) {
            // Skip the test if there are no chat groups
            return
        }
    
        val chatGroupId = chatGroup._id
    
        composeTestRule.setContent {
            NavHost(
                navController = navController,
                startDestination = FacebookScreen.HOME.name
            ) {
                composable(FacebookScreen.HOME.name) {
                    HomeScreen(navController = navController, userViewModel = userViewModel, homeViewModel = homeViewModel)
                }
                composable("${FacebookScreen.CHAT_GROUP.name}/{chatGroupId}") { backStackEntry ->
                    val id = backStackEntry.arguments?.getString("chatGroupId")
                    ChatGroupScreen(navController = navController)
                }
            }
        }
    
        // Wait for the UI to be idle
        composeTestRule.waitForIdle()
    
        // Find and click the first chat group item
        composeTestRule.onNodeWithText(chatGroup.name).performClick()
    
        // Wait for the navigation to occur
        composeTestRule.waitForIdle()
    
        // Assert that navigation occurred
        assertEquals("${FacebookScreen.CHAT_GROUP.name}/$chatGroupId", navController.currentDestination?.route)
    }
}