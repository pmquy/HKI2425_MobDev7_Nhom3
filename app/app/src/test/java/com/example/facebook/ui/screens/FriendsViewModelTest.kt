package com.example.facebook.ui.screens

import com.example.facebook.FacebookApplication
import com.example.facebook.data.FriendRepository
import com.example.facebook.model.Friend
import com.example.facebook.model.GetFriendResponse
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.unmockkAll
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class FriendsViewModelTest {

    private val application = mockk<FacebookApplication>(relaxed = true)
    private val friendRepository = mockk<FriendRepository>()
    private lateinit var viewModel: FriendsViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = FriendsViewModel(friendRepository, application)
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `verify initial subScreen`() = runTest {
        assertEquals(FriendSubScreen.SUGGESTS, viewModel.uiState.first().currentSubScreen)
    }

    @Test
    fun `verify subScreen change`() = runTest {
        viewModel.changeSubScreen(FriendSubScreen.ALL)
        assertEquals(FriendSubScreen.ALL, viewModel.uiState.first().currentSubScreen)
    }

    @Test
    fun `search updates search text`() = runTest {
        val searchText = "test search"
        viewModel.setSearch(searchText)
        assertEquals(searchText, viewModel.uiState.first().search)
    }

    @Test
    fun `clearInput resets search text`() = runTest {
        viewModel.setSearch("something")
        viewModel.clearInput()
        assertEquals("", viewModel.uiState.first().search)
    }

    @Test
    fun `getFriends updates friends list`() = runTest {
        val mockFriends = listOf(Friend(from = "user1", to = "user2", status = "accepted"))
        val getFriendResponse = GetFriendResponse(data = mockFriends, hasMore = false)

        coEvery { friendRepository.getAll(0, 100, any()) } returns Response.success(
            getFriendResponse
        )

        viewModel.getFriends()
        testDispatcher.scheduler.runCurrent()

        val uiState = viewModel.uiState.first()

        assertEquals(1, uiState.friends.size)
        assertEquals("user1", uiState.friends[0].from)
    }

    @Test
    fun `getRequests fetches and updates requests list`() = runTest {
        val mockRequests = listOf(Friend(from = "user1", to = "user2", status = "pending"))
        val requestResponse = GetFriendResponse(data = mockRequests, hasMore = false)

        coEvery {
            friendRepository.getAll(0, 100, any())
        } returns Response.success(requestResponse)

        viewModel.getRequests()
        testDispatcher.scheduler.runCurrent()

        val uiState = viewModel.uiState.first()
        assertEquals(1, uiState.requests.size)
        assertEquals("user1", uiState.requests[0].from)
    }


    @Test
    fun `accept request should progress to a friend`() = runTest {
        val friendId = "user1"
        coEvery { friendRepository.accept(friendId) } returns Unit
        viewModel.accept(friendId)
    }

    @Test
    fun `revoke removes sent request`() = runTest {
        val userId = "user1"
        coEvery { friendRepository.revoke(userId) } returns Unit
        viewModel.revoke(userId)
    }
}