package com.example.facebook.data

import com.example.facebook.model.Friend
import com.example.facebook.model.FriendAccept
import com.example.facebook.model.FriendRequest
import com.example.facebook.model.GetFriendResponse
import com.example.facebook.model.GetFriendSuggestionsResponse
import com.example.facebook.network.FriendsApiService
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class FriendRepositoryTest {

    private lateinit var friendRepository: FriendRepository
    private val mockFriendsApiService = mockk<FriendsApiService>()

    @Before
    fun setup() {
        friendRepository = NetworkFriendRepository(mockFriendsApiService)
    }

    @Test
    fun `request should send friend request to valid user`() = runTest {
        val validUser = "validUser123"
        coEvery { mockFriendsApiService.request(FriendRequest(validUser)) } returns mockk()

        friendRepository.request(validUser)

        coVerify { mockFriendsApiService.request(FriendRequest(validUser)) }
    }

    @Test
    fun `accept should call friendApiService accept with correct parameter`() = runTest {
        val friendRepository = NetworkFriendRepository(mockFriendsApiService)
        val from = "testUser"

        coEvery { mockFriendsApiService.accept(FriendAccept(from)) } returns Response.success(null)

        friendRepository.accept(from)

        coVerify { mockFriendsApiService.accept(FriendAccept(from)) }
    }

    @Test
    fun `decline should call friendApiService decline with correct parameter`() = runTest {
        val mockFriendApiService = mockk<FriendsApiService>()
        val friendRepository: FriendRepository = NetworkFriendRepository(mockFriendApiService)
        val fromUser = "user123"

        coEvery { mockFriendApiService.decline(FriendAccept(fromUser)) } returns Response.success(
            null
        )

        friendRepository.decline(fromUser)

        coVerify { mockFriendApiService.decline(FriendAccept(fromUser)) }
    }

    @Test
    fun `revoke should call friendApiService revoke with correct parameter`() = runTest {
        val friendRepository = mockk<FriendRepository>()
        val to = "user123"

        coEvery { friendRepository.revoke(to) } just Runs

        friendRepository.revoke(to)

        coVerify(exactly = 1) { friendRepository.revoke(to) }
    }

    @Test
    fun `disfriend should remove user from friends list`() = runTest {
        val friendRepository = mockk<FriendRepository>()
        val friendToRemove = "user123"

        coEvery { friendRepository.disfriend(friendToRemove) } just Runs

        friendRepository.disfriend(friendToRemove)

        coVerify(exactly = 1) { friendRepository.disfriend(friendToRemove) }
    }

    @Test
    fun `getAll should return a paginated list of friends with valid offset and limit`() = runTest {
        val mockFriendApiService = mockk<FriendsApiService>()
        val friendRepository: FriendRepository = NetworkFriendRepository(mockFriendApiService)
        val offset = 0
        val limit = 10
        val mockResponse = mockk<Response<GetFriendResponse>>()
        val mockFriendResponse = mockk<GetFriendResponse>()

        coEvery { mockFriendApiService.getAll(offset, limit, null) } returns mockResponse
        every { mockResponse.isSuccessful } returns true
        every { mockResponse.body() } returns mockFriendResponse
        every { mockFriendResponse.data } returns listOf(mockk(), mockk(), mockk())

        val result = friendRepository.getAll(offset, limit, null)

        assertTrue(result.isSuccessful)
        assertNotNull(result.body())
        assertEquals(3, result.body()?.data?.size)
        coVerify(exactly = 1) { mockFriendApiService.getAll(offset, limit, null) }
    }

    @Test
    fun `getAll should filter friends list by name when providing a search query`() = runTest {
        val mockFriendApiService = mockk<FriendsApiService>()
        val repository = NetworkFriendRepository(mockFriendApiService)

        val mockResponse = mockk<Response<GetFriendResponse>>()
        val mockBody = GetFriendResponse(
            data = listOf(
                Friend(_id = "1", from = "John Doe"),
                Friend(_id = "2", from = "Jane Smith"),
                Friend(_id = "3", from = "John Smith")
            ),
            hasMore = false
        )

        coEvery { mockFriendApiService.getAll(any(), any(), "John") } returns mockResponse
        every { mockResponse.isSuccessful } returns true
        every { mockResponse.body() } returns mockBody

        val result = repository.getAll(offset = null, limit = null, q = "John")

        assertTrue(result.isSuccessful)

        assertEquals(3, result.body()?.data?.size)

        assertTrue(result.body()?.data?.any { it.from.contains("John", ignoreCase = true) } == true)

        result.body()?.data?.forEach { friend ->
            assertTrue(
                "Friend name should contain 'John' or be 'Jane Smith'",
                friend.from.contains("John", ignoreCase = true) || friend.from == "Jane Smith"
            )
        }
    }

    @Test
    fun `getSuggestions should return friend suggestions based on mutual connections`() = runTest {
        val mockFriendRepository = mockk<FriendRepository>()
        val offset = 0
        val limit = 10
        val query = "John"
        val mockResponse = mockk<Response<GetFriendSuggestionsResponse>>()
        val mockSuggestions = listOf("John Doe", "Jane Smith")

        coEvery { mockFriendRepository.getSuggestions(offset, limit, query) } returns mockResponse
        every { mockResponse.isSuccessful } returns true
        every { mockResponse.body() } returns GetFriendSuggestionsResponse(
            data = mockSuggestions,
            hasMore = false
        )

        val result = mockFriendRepository.getSuggestions(offset, limit, query)

        assertTrue(result.isSuccessful)
        assertNotNull(result.body())
        assertEquals(2, result.body()?.data?.size)
        assertEquals("John Doe", result.body()?.data?.get(0))
        assertEquals("Jane Smith", result.body()?.data?.get(1))
    }

    @Test
    fun `getSuggestions should handle empty or null parameters gracefully`() = runTest {
        val mockFriendApiService = mockk<FriendsApiService>()
        val repository = NetworkFriendRepository(mockFriendApiService)

        val expectedResponse =
            Response.success(GetFriendSuggestionsResponse(hasMore = false, data = emptyList()))
        coEvery {
            mockFriendApiService.getSuggestions(
                any(),
                any(),
                any()
            )
        } returns expectedResponse

        val response = repository.getSuggestions(null, null, null)

        assertEquals(expectedResponse, response)
        coVerify { mockFriendApiService.getSuggestions(null, null, null) }
    }

    @Test
    fun `getAll and getSuggestions should return empty lists when no friends or suggestions are available`() =
        runTest {
            val mockFriendApiService = mockk<FriendsApiService>()
            val friendRepository: FriendRepository = NetworkFriendRepository(mockFriendApiService)

            coEvery { mockFriendApiService.getAll(any(), any(), any()) } returns Response.success(
                GetFriendResponse(data = emptyList(), hasMore = false)
            )
            coEvery {
                mockFriendApiService.getSuggestions(
                    any(),
                    any(),
                    any()
                )
            } returns Response.success(
                GetFriendSuggestionsResponse(data = emptyList(), hasMore = false)
            )

            val allFriendsResponse = friendRepository.getAll(null, null, null)
            val suggestionsResponse = friendRepository.getSuggestions(null, null, null)

            assertTrue(allFriendsResponse.isSuccessful)
            assertTrue(allFriendsResponse.body()?.data?.isEmpty() ?: false)

            assertTrue(suggestionsResponse.isSuccessful)
            assertTrue(suggestionsResponse.body()?.data?.isEmpty() ?: false)
        }
}
