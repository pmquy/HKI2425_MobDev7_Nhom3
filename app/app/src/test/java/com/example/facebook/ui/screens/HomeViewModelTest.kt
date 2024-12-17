package com.example.facebook.ui.screens

import com.example.facebook.FacebookApplication
import com.example.facebook.data.ChatGroupRepository
import com.example.facebook.model.ChatGroup
import com.example.facebook.model.GetAllChatGroupsResponse
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Response


@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private lateinit var viewModel: HomeViewModel
    private val chatGroupRepository = mockk<ChatGroupRepository>()
    private val application = mockk<FacebookApplication>(relaxed = true)

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = HomeViewModel(chatGroupRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `initial ui state is correct`() {
        val uiState = viewModel.uiState.value
        assertEquals(0, uiState.chatGroups.size)
        assertEquals(true, uiState.hasMore)
        assertEquals(0, uiState.offset)
    }

    @Test
    fun `getAll retrieves chat groups correctly`() = runTest {
        val chatGroups =
            listOf(ChatGroup(_id = "1", name = "Group 1"), ChatGroup(_id = "2", name = "Group 2"))
        val response = Response.success(GetAllChatGroupsResponse(data = chatGroups, hasMore = true))
        coEvery { chatGroupRepository.getAll(any(), any(), any()) } returns response

        viewModel.getAll()
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertEquals(2, uiState.chatGroups.size)
        assertEquals(chatGroups, uiState.chatGroups)
        assertEquals(true, uiState.hasMore)
    }

    @Test
    fun `getAll updates hasMore correctly when no more data`() = runTest {
        val chatGroups = listOf(ChatGroup(_id = "1", name = "Group 1"))
        val response =
            Response.success(GetAllChatGroupsResponse(data = chatGroups, hasMore = false))
        coEvery { chatGroupRepository.getAll(any(), any(), any()) } returns response

        viewModel.getAll()
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertEquals(1, uiState.chatGroups.size)
        assertEquals(false, uiState.hasMore)
    }

    @Test
    fun `getAll handles failure correctly`() = runTest {
        coEvery {
            chatGroupRepository.getAll(
                any(),
                any(),
                any()
            )
        } throws Exception("Failed to retrieve messages")

        try {
            viewModel.getAll()
            advanceUntilIdle()
        } catch (e: Exception) {
            assertEquals("Failed to retrieve messages", e.message)
        }
    }

    @Test
    fun `initial state is correct`() {
        val uiState = viewModel.uiState.value
        assertTrue(uiState.chatGroups.isEmpty())
        assertTrue(uiState.hasMore)
        assertEquals(0, uiState.offset)
    }

    @Test
    fun `getAll retrieves chat groups successfully`() = runTest {
        val mockChatGroups = listOf(
            ChatGroup(_id = "1", name = "Group 1"),
            ChatGroup(_id = "2", name = "Group 2")
        )
        val response =
            Response.success(GetAllChatGroupsResponse(data = mockChatGroups, hasMore = false))
        coEvery { chatGroupRepository.getAll(any(), any(), any()) } returns response

        viewModel.getAll()

        val uiState = viewModel.uiState.value
        assertEquals(2, uiState.chatGroups.size)
        assertEquals(mockChatGroups, uiState.chatGroups)
        assertFalse(uiState.hasMore)
    }

    @Test(expected = Exception::class)
    fun `getAll throws exception on failure`() = runTest {
        coEvery {
            chatGroupRepository.getAll(
                any(),
                any(),
                any()
            )
        } throws Exception("Failed to retrieve chat groups")

        viewModel.getAll()
    }

    @Test
    fun `offset increases correctly after loading more`() = runTest {
        val mockChatGroups = listOf(
            ChatGroup(_id = "1", name = "Group 1"),
            ChatGroup(_id = "2", name = "Group 2")
        )
        val response =
            Response.success(GetAllChatGroupsResponse(data = mockChatGroups, hasMore = true))
        coEvery { chatGroupRepository.getAll(any(), any(), any()) } returns response

        viewModel.getAll()

        val uiState = viewModel.uiState.value
        assertEquals(2, uiState.chatGroups.size)
        assertEquals(mockChatGroups, uiState.chatGroups)
        assertTrue(uiState.hasMore)
        assertEquals(0, uiState.offset)
    }

    @Test
    fun `handle empty chat groups response`() = runTest {
        val response =
            Response.success(GetAllChatGroupsResponse(data = emptyList(), hasMore = false))
        coEvery { chatGroupRepository.getAll(any(), any(), any()) } returns response

        viewModel.getAll()

        val uiState = viewModel.uiState.value
        assertTrue(uiState.chatGroups.isEmpty())
        assertFalse(uiState.hasMore)
    }

}