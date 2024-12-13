package com.example.facebook.ui.screens

import android.content.Context
import android.widget.Toast
import com.example.facebook.FacebookApplication
import com.example.facebook.data.UserRepository
import com.example.facebook.model.GetUsersResponse
import com.example.facebook.model.User
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Response

@ExperimentalCoroutinesApi
class FindUserViewModelTest {

    private lateinit var findUserViewModel: FindUserViewModel
    private lateinit var userRepository: UserRepository
    private lateinit var application: FacebookApplication

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        userRepository = mockk()
        application = mockk(relaxed = true)

        findUserViewModel = FindUserViewModel(application, userRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `setSearch updates search term`() {
        val searchTerm = "John"
        findUserViewModel.setSearch(searchTerm)
        assertEquals(searchTerm, findUserViewModel.uiState.value.search)
    }

    @Test
    fun `clearInput resets search term and user list`() {
        findUserViewModel.setSearch("Test")
        findUserViewModel.clearInput()
        val uiState = findUserViewModel.uiState.value
        assertEquals("", uiState.search)
        assertTrue(uiState.users.isEmpty())
    }

    @Test
    fun `findUsers cancels previous search job on new search`() {
        findUserViewModel.setSearch("First")
        val firstSearchJob = findUserViewModel.searchJob

        findUserViewModel.setSearch("Second")
        val secondSearchJob = findUserViewModel.searchJob

        assertTrue(firstSearchJob?.isCancelled == true)
        assertFalse(secondSearchJob?.isCancelled == true)
    }

    @Test
    fun `clearInput should reset search and users`() = runTest {
        findUserViewModel.clearInput()

        assertTrue(findUserViewModel.uiState.value.search.isEmpty())
        assertTrue(findUserViewModel.uiState.value.users.isEmpty())
    }

    @Test
    fun `test initial UI state`() {
        assertEquals("", findUserViewModel.uiState.value.search)
        assertTrue(findUserViewModel.uiState.value.users.isEmpty())
        assertFalse(findUserViewModel.uiState.value.hasMore)
        assertEquals(0, findUserViewModel.uiState.value.offset)
    }

}
