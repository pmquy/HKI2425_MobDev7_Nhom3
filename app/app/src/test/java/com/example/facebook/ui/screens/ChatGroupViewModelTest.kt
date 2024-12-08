//package com.example.facebook.ui.screens
//
//import java.io.File
//import androidx.arch.core.executor.testing.InstantTaskExecutorRule
//import androidx.lifecycle.Observer
//import com.example.facebook.FacebookApplication
//import com.example.facebook.data.ChatGroupRepository
//import com.example.facebook.data.MessageRepository
//import com.example.facebook.data.SocketRepository
//import com.example.facebook.data.UserRepository
//import com.example.facebook.model.ChatGroup
//import com.example.facebook.model.Member
//import com.example.facebook.model.Message
//import io.mockk.*
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.ExperimentalCoroutinesApi
//import kotlinx.coroutines.test.*
//import org.junit.After
//import org.junit.Assert.assertFalse
//import org.junit.Assert.assertTrue
//import org.junit.Before
//import org.junit.Rule
//import org.junit.Test
//
//@ExperimentalCoroutinesApi
//class ChatGroupViewModelTest {
//
//    @get:Rule
//    val instantExecutorRule = InstantTaskExecutorRule()
//
//    private lateinit var viewModel: ChatGroupViewModel
//    private lateinit var application: FacebookApplication
//    private lateinit var chatGroupRepository: ChatGroupRepository
//    private lateinit var userRepository: UserRepository
//    private lateinit var messageRepository: MessageRepository
//    private lateinit var socketRepository: SocketRepository
//
//    @Before
//    fun setup() {
//        Dispatchers.setMain(UnconfinedTestDispatcher())
//        application = mockk()
//        chatGroupRepository = mockk()
//        userRepository = mockk()
//        messageRepository = mockk()
//        socketRepository = mockk()
//
//        // Mock the addEventListener method
//        every { socketRepository.addEventListener(any(), any()) } just Runs
//
//        viewModel = ChatGroupViewModel(application, chatGroupRepository, userRepository, messageRepository, socketRepository)
//    }
//
//    @After
//    fun tearDown() {
//        Dispatchers.resetMain()
//    }
//
//    @Test
//    fun `initChatGroup should successfully initialize chat group and messages`() = runTest {
//        val chatGroupId = "123"
//        val chatGroup = ChatGroup(_id = chatGroupId)
//        val messages = listOf<Message>()
//        val members = listOf<Member>()
//
//        coEvery { socketRepository.sendMessage(any(), any(), any()) } returns Unit
//        coEvery { chatGroupRepository.getById(chatGroupId) } returns mockk { every { isSuccessful } returns true; every { body() } returns chatGroup }
//        coEvery { chatGroupRepository.getMessage(chatGroupId, 0, 10, "{}") } returns mockk { every { isSuccessful } returns true; every { body() } returns mockk { every { data } returns messages; every { hasMore } returns false } }
//        coEvery { chatGroupRepository.getMember(chatGroupId) } returns mockk { every { isSuccessful } returns true; every { body() } returns members }
//
//        viewModel.initChatGroup(chatGroupId)
//
//        assert(viewModel.uiState.value.chatGroup == chatGroup)
//        assert(viewModel.uiState.value.messages == messages)
//        assert(viewModel.uiState.value.users == members)
//    }
//
//    @Test(expected = Exception::class)
//    fun `initChatGroup should throw exception if chat group retrieval fails`() = runTest {
//        val chatGroupId = "123"
//        coEvery { chatGroupRepository.getById(chatGroupId) } returns mockk { every { isSuccessful } returns false }
//
//        viewModel.initChatGroup(chatGroupId)
//    }
//
//    @Test(expected = Exception::class)
//    fun `initChatGroup should throw exception if messages retrieval fails`() = runTest {
//        val chatGroupId = "123"
//        val chatGroup = ChatGroup(_id = chatGroupId)
//
//        coEvery { chatGroupRepository.getById(chatGroupId) } returns mockk { every { isSuccessful } returns true; every { body() } returns chatGroup }
//        coEvery { chatGroupRepository.getMessage(chatGroupId, 0, 10, "{}") } returns mockk { every { isSuccessful } returns false }
//
//        viewModel.initChatGroup(chatGroupId)
//    }
//
//    @Test(expected = Exception::class)
//    fun `initChatGroup should throw exception if members retrieval fails`() = runTest {
//        val chatGroupId = "123"
//        val chatGroup = ChatGroup(_id = chatGroupId)
//
//        coEvery { chatGroupRepository.getById(chatGroupId) } returns mockk { every { isSuccessful } returns true; every { body() } returns chatGroup }
//        coEvery { chatGroupRepository.getMessage(chatGroupId, 0, 10, "{}") } returns mockk { every { isSuccessful } returns true; every { body() } returns mockk { every { data } returns emptyList<Message>() } }
//        coEvery { chatGroupRepository.getMember(chatGroupId) } returns mockk { every { isSuccessful } returns false }
//
//        viewModel.initChatGroup(chatGroupId)
//    }
//
//    @Test
//    fun `initChatGroup should not update state if chat group ID is empty`() = runTest {
//        viewModel.initChatGroup("")
//
//        assert(viewModel.uiState.value.chatGroup == ChatGroup())
//        assert(viewModel.uiState.value.messages.isEmpty())
//        assert(viewModel.uiState.value.users.isEmpty())
//    }
//
//
//    @Test
//    fun `createMessage should reset message text and files after successful creation`() = runTest {
//        val messageText = "Hello"
//        val files = listOf<Pair<File, String>>()
//        val systemFiles = listOf<String>()
//
//        coEvery { messageRepository.create(messageText, any(), systemFiles, files) } returns mockk {
//            every { isSuccessful } returns true
//        }
//
//        viewModel.setMessageText(messageText)
//        viewModel.setFiles(files)
//        viewModel.setSystemFiles(systemFiles)
//
//        viewModel.createMessage(messageText, files, systemFiles)
//
//        assert(viewModel.uiState.value.messageText.isEmpty())
//        assert(viewModel.uiState.value.files.isEmpty())
//        assert(viewModel.uiState.value.systemFiles.isEmpty())
//    }
//
//    @Test
//    fun `addMember should add a new member to the users list`() {
//        val userId = "user1"
//        val role = "member"
//
//        viewModel.addMember(userId, role)
//
//        assert(viewModel.uiState.value.users.any { it.user == userId && it.role == role })
//    }
//
//    @Test
//    fun `removeMember should remove a member from the users list`() {
//        val userId = "user1"
//        viewModel.addMember(userId)
//
//        viewModel.removeMember(userId)
//
//        assert(!viewModel.uiState.value.users.any { it.user == userId })
//    }
//
//    @Test
//    fun `checkMember should return true if user is a member`() {
//        val userId = "user1"
//        viewModel.addMember(userId)
//
//        assert(viewModel.checkMember(userId))
//    }
//
//    @Test
//    fun `isMine should return true if message belongs to current user`() {
//        val currentUserId = "current_user"
//        every { application.user._id } returns currentUserId
//        val message = Message(user = currentUserId)
//
//        assert(viewModel.isMine(message))
//    }
//}
