package com.example.facebook.data

import com.example.facebook.model.ChatGroup
import com.example.facebook.model.GetAllChatGroupsResponse
import com.example.facebook.model.GetMessagesResponse
import com.example.facebook.model.Member
import com.example.facebook.network.ChatgroupApiService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.io.File

class NetworkChatGroupRepositoryTest {

    private lateinit var repository: NetworkChatGroupRepository
    private val mockChatgroupApiService = mockk<ChatgroupApiService>()

    @Before
    fun setup() {
        repository = NetworkChatGroupRepository(mockChatgroupApiService)
    }

    @Test
    fun `should successfully create a chat group with valid name, users, and avatar`() = runTest {
        // Arrange
        val name = "Test Group"
        val users = listOf(Member("user1", "member"), Member("user2", "admin"))
        val avatarFile = mockk<File>()
        every { avatarFile.name } returns "avatar.jpg"
        val avatar = Pair(avatarFile, "image/jpeg")

        val mockChatGroup = mockk<ChatGroup>()
        every { mockChatGroup._id } returns "testId"
        every { mockChatGroup.name } returns name

        val expectedResponse = mockk<Response<ChatGroup>>()
        every { expectedResponse.isSuccessful } returns true
        every { expectedResponse.body() } returns mockChatGroup

        coEvery {
            mockChatgroupApiService.create(
                any(),
                any(),
                any()
            )
        } returns expectedResponse

        // Act
        val result = repository.create(name, users, avatar)

        // Assert
        coVerify {
            mockChatgroupApiService.create(
                any(),
                any(),
                any()
            )
        }
        assertEquals(expectedResponse, result)
    }

    @Test
    fun `updateById should update chat group name and avatar successfully`() = runTest {
        // Arrange
        val chatgroupApiService = mockk<ChatgroupApiService>()
        val repository = NetworkChatGroupRepository(chatgroupApiService)
        val id = "testId"
        val name = "New Group Name"
        val avatarFile = File("test_avatar.jpg")
        val avatarMimeType = "image/jpeg"
        val avatar = Pair(avatarFile, avatarMimeType)
        val updatedChatGroup = ChatGroup(_id = id, name = name)

        val nameRequestBody = name.toRequestBody("text/plain".toMediaType())
        val avatarPart = MultipartBody.Part.createFormData(
            "avatar",
            avatarFile.name,
            avatarFile.asRequestBody(avatarMimeType.toMediaType())
        )

        coEvery {
            chatgroupApiService.updateById(any(), any(), any())
        } returns Response.success(updatedChatGroup)

        // Act
        val result = repository.updateById(id, name, avatar)

        // Assert
        assertTrue(result.isSuccessful)
        assertEquals(updatedChatGroup, result.body())
        coVerify(exactly = 1) {
            chatgroupApiService.updateById(id, any(), any())
        }
    }

    @Test
    fun `updateById should throw exception when update fails`() = runTest {
        val chatgroupApiService = mockk<ChatgroupApiService>()
        val repository = NetworkChatGroupRepository(chatgroupApiService)
        val id = "testId"
        val name = "Test Group"
        val avatar = Pair(File("path/to/avatar.jpg"), "image/jpeg")

        coEvery {
            chatgroupApiService.updateById(
                id,
                any(),
                any()
            )
        } returns Response.error(400, "".toResponseBody())

        val exception = assertThrows(Exception::class.java) {
            runBlocking {
                repository.updateById(id, name, avatar)
            }
        }

        assertTrue(exception.message?.contains("Error updating chat group") == true)
    }

    @Test
    fun `getAll should retrieve chat groups with correct pagination and query`() = runTest {
        val offset = 0
        val limit = 10
        val query = "test"
        val mockResponse = mockk<Response<GetAllChatGroupsResponse>>()
        val mockBody = mockk<GetAllChatGroupsResponse>()

        coEvery { mockChatgroupApiService.getAll(offset, limit, query) } returns mockResponse
        every { mockResponse.isSuccessful } returns true
        every { mockResponse.body() } returns mockBody

        val result = repository.getAll(offset, limit, query)

        coVerify(exactly = 1) { mockChatgroupApiService.getAll(offset, limit, query) }
        assertEquals(mockResponse, result)
    }

    @Test
    fun `getMessage should fetch messages for a specific chat group with proper offset and limit`() =
        runTest {
            val chatgroupApiService = mockk<ChatgroupApiService>()
            val repository = NetworkChatGroupRepository(chatgroupApiService)

            val id = "testId"
            val offset = 0
            val limit = 10
            val query = ""

            val mockResponse = mockk<Response<GetMessagesResponse>> {
                every { isSuccessful } returns true
                every { body() } returns GetMessagesResponse(emptyList(), false)
            }

            coEvery {
                chatgroupApiService.getMessage(
                    id,
                    offset,
                    limit,
                    query
                )
            } returns mockResponse

            val result = repository.getMessage(id, offset, limit, query)

            coVerify(exactly = 1) { chatgroupApiService.getMessage(id, offset, limit, query) }
            assertEquals(mockResponse, result)
        }

    @Test
    fun `should add multiple members to an existing chat group`() = runTest {
        // Arrange
        val chatGroupId = "testChatGroupId"
        val members = listOf(
            Member("user1", "member"),
            Member("user2", "member")
        )
        val expectedResponse = mockk<Response<ChatGroup>> {
            every { isSuccessful } returns true
            every { body() } returns ChatGroup(_id = chatGroupId, name = "Test Group")
        }
        coEvery {
            mockChatgroupApiService.addMembers(
                chatGroupId,
                members
            )
        } returns expectedResponse

        // Act
        val result = repository.addMember(chatGroupId, members)

        // Assert
        coVerify { mockChatgroupApiService.addMembers(chatGroupId, members) }
        assertEquals(expectedResponse, result)
        assertTrue(result.isSuccessful)
        assertNotNull(result.body())
        assertEquals(chatGroupId, result.body()?._id)
    }

    @Test
    fun `removeMember should call chatgroupApiService removeMember with correct parameters`() =
        runTest {
            val chatgroupApiService = mockk<ChatgroupApiService>()
            val repository = NetworkChatGroupRepository(chatgroupApiService)
            val id = "testGroupId"
            val member = Member("testUser", "member")
            val mockResponse = mockk<Response<ChatGroup>>()

            coEvery { chatgroupApiService.removeMember(id, member) } returns mockResponse

            val result = repository.removeMember(id, member)

            coVerify { chatgroupApiService.removeMember(id, member) }
            assertEquals(mockResponse, result)
        }

    @Test
    fun `updateMember should call chatgroupApiService updateMember with correct parameters`() =
        runTest {
            val chatgroupApiService = mockk<ChatgroupApiService>()
            val repository = NetworkChatGroupRepository(chatgroupApiService)
            val id = "testId"
            val member = Member("user1", "admin")
            val expectedResponse = mockk<Response<ChatGroup>>()

            coEvery { chatgroupApiService.updateMember(id, member) } returns expectedResponse

            val result = repository.updateMember(id, member)

            coVerify { chatgroupApiService.updateMember(id, member) }
            assertEquals(expectedResponse, result)
        }

    @Test
    fun `getMember should return list of members for a specific chat group`() = runTest {
        val chatGroupId = "testGroupId"
        val expectedMembers = listOf(
            Member("user1", "admin"),
            Member("user2", "member")
        )
        val mockResponse = Response.success(expectedMembers)

        coEvery { mockChatgroupApiService.getMember(chatGroupId) } returns mockResponse

        val repository = NetworkChatGroupRepository(mockChatgroupApiService)
        val result = repository.getMember(chatGroupId)

        assertTrue(result.isSuccessful)
        assertEquals(expectedMembers, result.body())
    }
}
