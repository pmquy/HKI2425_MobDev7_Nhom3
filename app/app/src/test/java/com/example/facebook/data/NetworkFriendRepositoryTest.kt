package com.example.facebook.data

import com.example.facebook.model.FriendAccept
import com.example.facebook.model.FriendRequest
import com.example.facebook.model.GetFriendResponse
import com.example.facebook.model.GetFriendSuggestionsResponse
import com.example.facebook.network.FriendsApiService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class NetworkFriendRepositoryTest {

    private lateinit var repository: NetworkFriendRepository
    private val mockFriendApiService: FriendsApiService = mockk()

    @Before
    fun setup() {
        repository = NetworkFriendRepository(mockFriendApiService)
    }

    @Test
    fun `request should call friendApiService with correct parameters`() = runTest {
        val to = "validUser"
        coEvery { mockFriendApiService.request(FriendRequest(to)) } returns mockk()

        repository.request(to)

        coVerify { mockFriendApiService.request(FriendRequest(to)) }
    }

    @Test
    fun `should accept a friend request from an existing user`() = runTest {
        // Arrange
        val friendApiService = mockk<FriendsApiService>()
        val networkFriendRepository = NetworkFriendRepository(friendApiService)
        val existingUser = "existingUser"

        coEvery { friendApiService.accept(FriendAccept(existingUser)) } returns Response.success(
            null
        )

        // Act
        networkFriendRepository.accept(existingUser)

        // Assert
        coVerify(exactly = 1) { friendApiService.accept(FriendAccept(existingUser)) }
    }

    @Test
    fun `decline should call friendApiService decline with correct parameters`() = runTest {
        val mockFriendApiService = mockk<FriendsApiService>()
        val repository = NetworkFriendRepository(mockFriendApiService)
        val fromUser = "user123"

        coEvery { mockFriendApiService.decline(FriendAccept(fromUser)) } returns Response.success(
            null
        )

        repository.decline(fromUser)

        coVerify { mockFriendApiService.decline(FriendAccept(fromUser)) }
    }

    @Test
    fun `revoke should call friendApiService revoke with correct parameter`() = runTest {
        val friendApiService = mockk<FriendsApiService>()
        val repository = NetworkFriendRepository(friendApiService)
        val to = "user123"

        coEvery { friendApiService.revoke(any()) } returns Response.success(null)

        repository.revoke(to)

        coVerify { friendApiService.revoke(FriendRequest(to)) }
    }

    @Test
    fun `disfriend should call friendApiService disfriend with correct parameter`() = runTest {
        val friendApiService = mockk<FriendsApiService>()
        val repository = NetworkFriendRepository(friendApiService)
        val from = "user123"

        coEvery { friendApiService.disfriend(FriendAccept(from)) } returns Response.success(null)

        repository.disfriend(from)

        coVerify { friendApiService.disfriend(FriendAccept(from)) }
    }

    @Test
    fun `getAll should retrieve all friends with default pagination`() = runTest {
        // Arrange
        val mockResponse = mockk<Response<GetFriendResponse>>()
        val mockFriendsApiService = mockk<FriendsApiService>()
        coEvery { mockFriendsApiService.getAll(null, null, null) } returns mockResponse

        val repository = NetworkFriendRepository(mockFriendsApiService)

        // Act
        val result = repository.getAll(null, null, null)

        // Assert
        coVerify { mockFriendsApiService.getAll(null, null, null) }
        assertEquals(mockResponse, result)
    }


    @Test
    fun `getAll should retrieve friends with custom offset and limit`() = runTest {
        val offset = 10
        val limit = 20
        val mockResponse = mockk<Response<GetFriendResponse>>()

        coEvery { mockFriendApiService.getAll(offset, limit, null) } returns mockResponse

        val result = repository.getAll(offset, limit, null)

        coVerify { mockFriendApiService.getAll(offset, limit, null) }
        assertEquals(mockResponse, result)
    }

    @Test
    fun `getAll should filter friends list based on search query`() = runTest {
        // Arrange
        val friendApiService = mockk<FriendsApiService>()
        val repository = NetworkFriendRepository(friendApiService)
        val mockResponse = mockk<Response<GetFriendResponse>>()
        val searchQuery = "John"

        coEvery { friendApiService.getAll(any(), any(), eq(searchQuery)) } returns mockResponse

        // Act
        val result = repository.getAll(offset = null, limit = null, q = searchQuery)

        // Assert
        coVerify { friendApiService.getAll(null, null, searchQuery) }
        assertEquals(mockResponse, result)
    }

    @Test
    fun `getSuggestions should call friendApiService getSuggestions with default pagination`() =
        runTest {
            // Arrange
            val mockFriendApiService = mockk<FriendsApiService>()
            val repository = NetworkFriendRepository(mockFriendApiService)
            val mockResponse = mockk<Response<GetFriendSuggestionsResponse>>()

            coEvery { mockFriendApiService.getSuggestions(null, null, null) } returns mockResponse

            // Act
            val result = repository.getSuggestions(null, null, null)

            // Assert
            coVerify { mockFriendApiService.getSuggestions(null, null, null) }
            assertEquals(mockResponse, result)
        }

    @Test
    fun `getSuggestions should return friend suggestions with custom parameters`() = runTest {
        val offset = 10
        val limit = 20
        val query = "John"
        val mockResponse = mockk<Response<GetFriendSuggestionsResponse>>()
        val mockFriendApiService = mockk<FriendsApiService>()

        coEvery { mockFriendApiService.getSuggestions(offset, limit, query) } returns mockResponse

        val repository = NetworkFriendRepository(mockFriendApiService)
        val result = repository.getSuggestions(offset, limit, query)

        coVerify { mockFriendApiService.getSuggestions(offset, limit, query) }
        assertEquals(mockResponse, result)
    }
}
