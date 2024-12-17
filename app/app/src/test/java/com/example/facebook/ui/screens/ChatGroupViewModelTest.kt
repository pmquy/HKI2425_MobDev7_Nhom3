package com.example.facebook.ui.screens

import com.example.facebook.FacebookApplication
import com.example.facebook.data.ChatGroupRepository
import com.example.facebook.data.MessageRepository
import com.example.facebook.data.SocketRepository
import com.example.facebook.data.UserRepository
import com.example.facebook.model.ChatGroup
import com.example.facebook.model.Message
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class ChatGroupViewModelTest {

    private lateinit var viewModel: ChatGroupViewModel
    private val chatGroupRepository = mockk<ChatGroupRepository>()
    private val messageRepository = mockk<MessageRepository>()
    private val userRepository = mockk<UserRepository>()
    private val socketRepository = mockk<SocketRepository>(relaxed = true)
    private val application = mockk<FacebookApplication>(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { application.user._id } returns "testUserId"
        viewModel = ChatGroupViewModel(
            application,
            chatGroupRepository,
            userRepository,
            messageRepository,
            socketRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `getMoreMessages should not fetch messages when hasMore is false`() = runTest {
        viewModel.getMoreMessages()
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertTrue(uiState.messages.isEmpty())
    }

    @Test
    fun `setSystemFiles should update systemFiles correctly`() {
        val systemFiles = listOf("file1", "file2")
        viewModel.setSystemFiles(systemFiles)
        assertEquals(systemFiles, viewModel.uiState.value.systemFiles)
    }

    @Test
    fun `createMessage should handle message creation successfully`() = runTest {
        val mockMessage = Message(_id = "1", message = "Sending", user = "testUserId")

        coEvery { messageRepository.create(any(), any(), any(), any()) } returns Response.success(
            mockMessage
        )

        viewModel.createMessage("Sending", listOf(), listOf())
        advanceUntilIdle()

        assertEquals("", viewModel.uiState.value.messageText)
        assertTrue(viewModel.uiState.value.files.isEmpty())
        assertTrue(viewModel.uiState.value.systemFiles.isEmpty())
    }

    @Test
    fun `handleUpdate should update chatGroup info successfully`() = runTest {
        val updatedName = "Updated Name"
        val updatedChatGroup = ChatGroup(_id = "testChatGroupId", name = updatedName)

        coEvery { chatGroupRepository.updateById(any(), any(), any()) } returns Response.success(
            updatedChatGroup
        )

        viewModel.handleUpdate(updatedName, null)
        advanceUntilIdle()

        assertEquals(updatedChatGroup, viewModel.uiState.value.chatGroup)
    }

    @Test
    fun `setMessageText updates the message text in UI state`() {
        val messageText = "Hello, world!"
        viewModel.setMessageText(messageText)
        assertEquals(messageText, viewModel.uiState.value.messageText)
    }

    @Test
    fun `addMember and removeMember correctly manage members list`() {
        val userId = "user123"
        viewModel.addMember(userId)
        assertTrue(viewModel.checkMember(userId))

        viewModel.removeMember(userId)
        assertTrue(!viewModel.checkMember(userId))
    }

    @Test
    fun `setFiles should update files correctly`() {
        val files = listOf(Pair(mockk<java.io.File>(), "image/png"))
        viewModel.setFiles(files)
        assertEquals(files, viewModel.uiState.value.files)
    }

    @Test
    fun `isMine should correctly identify owned messages`() {
        val myMessage = Message(_id = "msg3", message = "My Message", user = "testUserId")
        val otherMessage = Message(_id = "msg4", message = "Others Message", user = "otherUserId")

        assertTrue(viewModel.isMine(myMessage))
        assertFalse(viewModel.isMine(otherMessage))
    }

    @Test
    fun `handleUpdate should correctly update chat group name and avatar`() = runTest {
        val updatedName = "New Group Name"
        val updatedChatGroup = ChatGroup(_id = "testChatGroupId", name = updatedName)

        coEvery { chatGroupRepository.updateById(any(), any(), any()) } returns Response.success(
            updatedChatGroup
        )

        viewModel.handleUpdate(updatedName, null)
        advanceUntilIdle()

        assertEquals(updatedName, viewModel.uiState.value.chatGroup.name)
    }

    @Test
    fun `addMember and checkMember should correctly manage the chat group's member list`() {
        val newMember = "newUserId"

        assertFalse(viewModel.checkMember(newMember))

        viewModel.addMember(newMember)
        assertTrue(viewModel.checkMember(newMember))
    }

}