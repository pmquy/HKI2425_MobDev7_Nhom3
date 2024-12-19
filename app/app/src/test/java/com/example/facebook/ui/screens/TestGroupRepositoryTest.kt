package com.example.facebook.ui.screens

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.facebook.data.ChatGroupRepository
import com.example.facebook.data.MessageRepository
import com.example.facebook.data.SocketRepository
import com.example.facebook.data.UserRepository
import com.example.facebook.model.Member
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class TestGroupViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: ChatGroupViewModel
    private val chatGroupRepository: ChatGroupRepository = mockk()
    private val userRepository: UserRepository = mockk()
    private val messageRepository: MessageRepository = mockk()
    private val socketRepository: SocketRepository = mockk()

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher) // Set the test dispatcher
        every { socketRepository.addEventListener(any(), any()) } just Runs
        viewModel = ChatGroupViewModel(
            application = mockk(relaxed = true),
            chatGroupRepository = chatGroupRepository,
            userRepository = userRepository,
            messageRepository = messageRepository,
            socketRepository = socketRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `add member should add member to the state users list`() = runTest(testDispatcher) {
        val member = Member("user123", "member")

        viewModel.addMember(member.user, member.role)

        assert(viewModel.uiState.value.users.contains(member))
    }
}