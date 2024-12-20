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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import retrofit2.Response
import java.io.File

class ChatGroupRepositoryTest {

    private lateinit var chatGroupRepository: ChatGroupRepository
    private val chatgroupApiService = mockk<ChatgroupApiService>()

    @Before
    fun setup() {
        chatGroupRepository = NetworkChatGroupRepository(chatgroupApiService)
    }

    @Test
    fun `getById should return a ChatGroup when given a valid ID`() = runTest {
        val mockChatGroup = mockk<ChatGroup>()
        val mockResponse = mockk<Response<ChatGroup>> {
            every { isSuccessful } returns true
            every { body() } returns mockChatGroup
        }

        coEvery { chatGroupRepository.getById("validId") } returns mockResponse

        val result = chatGroupRepository.getById("validId")

        assertTrue(result.isSuccessful)
        assertEquals(mockChatGroup, result.body())
    }

    @Test
    fun `create should return successful response with valid input`() = runTest {
        // Arrange
        val name = "Test Group"
        val users = listOf(Member("user1", "member"), Member("user2", "admin"))
        val avatarFile = File("test_avatar.jpg")
        val avatarMimeType = "image/jpeg"
        val avatar = Pair(avatarFile, avatarMimeType)
        val expectedChatGroup = ChatGroup(_id = "testId", name = name)

        coEvery {
            chatgroupApiService.create(
                any(),
                any(),
                any()
            )
        } returns Response.success(expectedChatGroup)

        // Act
        val result = chatGroupRepository.create(name, users, avatar)

        // Assert
        assertTrue(result.isSuccessful)
        assertEquals(expectedChatGroup, result.body())

        // Verify
        coVerify {
            chatgroupApiService.create(
                name = any(),
                users = any(),
                avatar = any()
            )
        }
    }

    @Test
    fun `updateById should update chat group name and avatar`() = runTest {
        val repository = mockk<ChatGroupRepository>()
        val id = "testId"
        val newName = "Updated Group Name"
        val newAvatar = Pair(File("new_avatar.jpg"), "image/jpeg")
        val updatedChatGroup = ChatGroup(_id = id, name = newName)

        coEvery { repository.updateById(id, newName, newAvatar) } returns Response.success(
            updatedChatGroup
        )

        val response = repository.updateById(id, newName, newAvatar)

        assertTrue(response.isSuccessful)
        assertEquals(updatedChatGroup, response.body())
        coVerify { repository.updateById(id, newName, newAvatar) }
    }

    @Test
    fun `deleteById should return successful response when deleting existing chat group`() =
        runTest {
            val chatGroupRepository = mockk<ChatGroupRepository>()
            val chatGroupId = "test-group-id"
            val response = mockk<Response<Void>>()

            coEvery { chatGroupRepository.deleteById(chatGroupId) } returns response
            every { response.isSuccessful } returns true

            val result = chatGroupRepository.deleteById(chatGroupId)

            coVerify(exactly = 1) { chatGroupRepository.deleteById(chatGroupId) }
            assertTrue(result.isSuccessful)
        }

    @Test
    fun `getAll should retrieve chat groups with pagination and search query`() = runTest {
        val mockResponse = mockk<Response<GetAllChatGroupsResponse>>()
        val mockChatGroups = listOf(
            ChatGroup(_id = "1", name = "Group 1"),
            ChatGroup(_id = "2", name = "Group 2")
        )
        val mockGetAllChatGroupsResponse = GetAllChatGroupsResponse(
            data = mockChatGroups,
            hasMore = true
        )

        every { mockResponse.isSuccessful } returns true
        every { mockResponse.body() } returns mockGetAllChatGroupsResponse

        coEvery { chatgroupApiService.getAll(any(), any(), any()) } returns mockResponse

        val result = chatGroupRepository.getAll(offset = 0, limit = 10, query = "test")

        coVerify { chatgroupApiService.getAll(0, 10, "test") }
        assertTrue(result.isSuccessful)
        assertEquals(mockGetAllChatGroupsResponse, result.body())
        assertEquals(2, result.body()?.data?.size)
        assertEquals("Group 1", result.body()?.data?.get(0)?.name)
        assertEquals("Group 2", result.body()?.data?.get(1)?.name)
    }

    @Test
    fun `getMessage should fetch messages for a specific chat group with pagination and search query`() =
        runTest {
            val chatGroupRepository = mockk<ChatGroupRepository>()
            val chatGroupId = "test_group_id"
            val offset = 0
            val limit = 10
            val query = "search_query"
            val mockResponse = mockk<Response<GetMessagesResponse>>()
            val mockGetMessagesResponse = mockk<GetMessagesResponse>()

            coEvery {
                chatGroupRepository.getMessage(
                    chatGroupId,
                    offset,
                    limit,
                    query
                )
            } returns mockResponse
            every { mockResponse.isSuccessful } returns true
            every { mockResponse.body() } returns mockGetMessagesResponse

            val result = chatGroupRepository.getMessage(chatGroupId, offset, limit, query)

            coVerify(exactly = 1) {
                chatGroupRepository.getMessage(
                    chatGroupId,
                    offset,
                    limit,
                    query
                )
            }
            assertTrue(result.isSuccessful)
            assertEquals(mockGetMessagesResponse, result.body())
        }

    @Test
    fun `addMember should add new members to an existing chat group`() = runTest {
        val chatGroupRepository = mockk<ChatGroupRepository>()
        val chatGroupId = "testGroupId"
        val newMembers = listOf(
            Member("user1", "member"),
            Member("user2", "member")
        )
        val updatedChatGroup = ChatGroup(_id = chatGroupId, name = "Test Group")

        coEvery { chatGroupRepository.addMember(chatGroupId, newMembers) } returns Response.success(
            updatedChatGroup
        )

        val response = chatGroupRepository.addMember(chatGroupId, newMembers)

        assertTrue(response.isSuccessful)
        assertEquals(updatedChatGroup, response.body())
        coVerify(exactly = 1) { chatGroupRepository.addMember(chatGroupId, newMembers) }
    }

    @Test
    fun `removeMember should remove a member from a chat group`() = runTest {
        val chatGroupRepository = mockk<ChatGroupRepository>()
        val chatGroupId = "testGroupId"
        val memberToRemove = Member("user123", "member")
        val updatedChatGroup = ChatGroup(_id = chatGroupId, name = "Test Group")

        coEvery {
            chatGroupRepository.removeMember(
                chatGroupId,
                memberToRemove
            )
        } returns Response.success(updatedChatGroup)

        val response = chatGroupRepository.removeMember(chatGroupId, memberToRemove)

        assertTrue(response.isSuccessful)
        assertEquals(updatedChatGroup, response.body())
        coVerify(exactly = 1) { chatGroupRepository.removeMember(chatGroupId, memberToRemove) }
    }

    @Test
    fun `updateMember should update member information in chat group`() = runTest {
        val chatGroupRepository = mockk<ChatGroupRepository>()
        val chatGroupId = "group123"
        val updatedMember = Member("user1", "admin")
        val updatedChatGroup = ChatGroup(_id = chatGroupId, name = "Test Group")

        coEvery {
            chatGroupRepository.updateMember(
                chatGroupId,
                updatedMember
            )
        } returns Response.success(updatedChatGroup)

        val response = chatGroupRepository.updateMember(chatGroupId, updatedMember)

        assertTrue(response.isSuccessful)
        assertEquals(updatedChatGroup, response.body())
        coVerify(exactly = 1) { chatGroupRepository.updateMember(chatGroupId, updatedMember) }
    }

    @Test
    fun `getMember should return list of members for a specific chat group`() = runTest {
        val chatGroupId = "testGroupId"
        val mockMembers = listOf(
            Member("user1", "member"),
            Member("user2", "admin")
        )
        val mockResponse = mockk<Response<List<Member>>> {
            every { isSuccessful } returns true
            every { body() } returns mockMembers
        }

        coEvery { chatGroupRepository.getMember(chatGroupId) } returns mockResponse

        val result = chatGroupRepository.getMember(chatGroupId)

        assertTrue(result.isSuccessful)
        assertEquals(mockMembers, result.body())
        coVerify(exactly = 1) { chatGroupRepository.getMember(chatGroupId) }
    }
}
