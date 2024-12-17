package com.example.facebook.ui.screens

import com.example.facebook.FacebookApplication
import com.example.facebook.data.ChatGroupRepository
import com.example.facebook.model.GetMessagesResponse
import com.example.facebook.model.Message
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.unmockkAll
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Response


@OptIn(ExperimentalCoroutinesApi::class)
class FindMessageViewModelTest {

    private lateinit var viewModel: FindMessageViewModel
    private val chatGroupRepository = mockk<ChatGroupRepository>()
    private val application = mockk<FacebookApplication>(relaxed = true)

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = FindMessageViewModel(chatGroupRepository, application)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `setChatGroupId updates chatGroupId correctly`() {
        val chatGroupId = "group1"
        viewModel.setChatGroupId(chatGroupId)
        assertEquals(chatGroupId, viewModel.uiState.value.chatGroupId)
    }

    @Test
    fun `setSearch updates search text correctly`() {
        val searchText = "hello"
        viewModel.setSearch(searchText)
        assertEquals(searchText, viewModel.uiState.value.search)
    }

    @Test
    fun `clearInput resets search and messages`() {
        viewModel.setSearch("hello")
        viewModel.clearInput()
        val uiState = viewModel.uiState.value
        assertEquals("", uiState.search)
        assertTrue(uiState.messages.isEmpty())
    }

    @Test
    fun `findMessages does not proceed without chatGroupId set`() = runTest {
        viewModel.setSearch("searchText")
        viewModel.findMessages().join()

        val uiState = viewModel.uiState.value
        assertTrue(uiState.messages.isEmpty())
        assertEquals("", uiState.chatGroupId)
    }

    @Test
    fun `setSearch cancels previous search job`() {
        viewModel.setSearch("first")
        val firstJob = viewModel.searchJob

        viewModel.setSearch("second")
        val secondJob = viewModel.searchJob

        assertTrue(firstJob?.isCancelled == true)
        assertFalse(secondJob?.isCancelled == true)
    }

    @Test
    fun `findMessages handles repository failure`() = runTest {
        coEvery {
            chatGroupRepository.getMessage(
                any(),
                any(),
                any(),
                any()
            )
        } throws Exception("Failed to retrieve messages")

        viewModel.setChatGroupId("group1")
        viewModel.setSearch("test")
        viewModel.findMessages().join()

        assertTrue(viewModel.uiState.value.messages.isEmpty())
        assertEquals("Failed to retrieve messages", viewModel.uiState.value.error)
    }

    @Test
    fun `search delay postpones execution by 500ms`() = runTest {
        viewModel.setChatGroupId("group1")
        viewModel.setSearch("delayed")

        val initialJobStatus = viewModel.searchJob?.isCompleted
        assertFalse(initialJobStatus == true)

        advanceTimeBy(499)
        runCurrent()

        val midJobStatus = viewModel.searchJob?.isCompleted
        assertFalse(midJobStatus == true)

        advanceTimeBy(1)
        runCurrent()

        val finalJobStatus = viewModel.searchJob?.isCompleted
        assertTrue(finalJobStatus == true)
    }

}
