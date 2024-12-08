package com.example.facebook.ui.screens

import com.example.facebook.FacebookApplication
import com.example.facebook.data.ChatGroupRepository
import com.example.facebook.model.ChatGroup
import com.example.facebook.model.Member
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class CreateChatGroupViewModelTest {

    private lateinit var viewModel: CreateChatGroupViewModel
    private val application = mockk<FacebookApplication>()
    private val chatGroupRepository = mockk<ChatGroupRepository>()

    @Before
    fun setup() {
        every { application.container.chatGroupRepository } returns chatGroupRepository
        viewModel = CreateChatGroupViewModel(application, chatGroupRepository)
    }

    @Test
    fun `setName should update uiState correctly`() {
        val name = "Test Group"
        viewModel.setName(name)
        assertEquals(name, viewModel.uiState.value.name)
    }

    @Test
    fun `setAvatar should update uiState correctly`() {
        val avatar = Pair(File("path1"), "name1")
        viewModel.setAvatar(avatar)
        assertEquals(avatar, viewModel.uiState.value.avatar)
    }

    @Test(expected = Exception::class)
    fun `createChatGroup should throw exception for empty name`() = runTest {
        viewModel.createChatGroup()
    }

    @Test(expected = Exception::class)
    fun `createChatGroup should throw exception for empty members`() = runTest {
        viewModel.setName("Test Group")
        viewModel.createChatGroup()
    }

    @Test
    fun `createChatGroup should call chatGroupRepository create with correct parameters`() = runTest {
        val name = "Test Group"
        val user = "user1"
        val avatar = Pair(File("path1"), "name1")
        val chatGroup = ChatGroup(_id = "testId", name = name)

        viewModel.setName(name)
        viewModel.addMember(user)
        viewModel.setAvatar(avatar)

        coEvery { chatGroupRepository.create(name, listOf(Member(user, "member")), avatar) } returns mockk {
            every { isSuccessful } returns true
            every { body() } returns chatGroup
        }

        viewModel.createChatGroup()
    }

    @Test
    fun `addMember should update uiState correctly`() {
        val user = "newUser"
        viewModel.addMember(user)
        assertTrue(viewModel.checkMember(user))
    }

    @Test
    fun `removeMember should update uiState correctly`() {
        val user = "userToRemove"
        viewModel.addMember(user)
        viewModel.removeMember(user)
        assertFalse(viewModel.checkMember(user))
    }

    @Test
    fun `checkMember should return correct result`() {
        val user = "userToCheck"
        viewModel.addMember(user)
        assertTrue(viewModel.checkMember(user))
        viewModel.removeMember(user)
        assertFalse(viewModel.checkMember(user))
    }
}
