package com.example.facebook.ui.screens

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.facebook.FacebookApplication
import com.example.facebook.data.FriendRepository
import com.example.facebook.model.Friend
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class FriendsViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: FriendsViewModel
    private lateinit var friendRepository: FriendRepository
    private val application: FacebookApplication = mockk(relaxed = true)

    @Before
    fun setUp() {
        friendRepository = mockk(relaxed = true)
        viewModel = FriendsViewModel(friendRepository, application)
    }

    @Test
    fun `test request friend functionality`() = runTest {
        val userId = "user1"
        viewModel.request(userId)

        advanceUntilIdle()

        coVerify { friendRepository.request(userId) }
    }

    @Test
    fun `test accept friend functionality`() = runTest {
        val userId = "user1"
        viewModel.accept(userId)

        advanceUntilIdle()

        coVerify { friendRepository.accept(userId) }
    }

    @Test
    fun `test decline friend functionality`() = runTest {
        val userId = "user1"
        viewModel.decline(userId)

        advanceUntilIdle()

        coVerify { friendRepository.decline(userId) }
    }

    @Test
    fun `test revoke friend request functionality`() = runTest {
        val userId = "user1"
        viewModel.revoke(userId)

        advanceUntilIdle()

        coVerify { friendRepository.revoke(userId) }
    }

    @Test
    fun `test disfriend functionality`() = runTest {
        val userId = "user1"
        viewModel.disfriend(userId)

        advanceUntilIdle()

        coVerify { friendRepository.disfriend(userId) }
    }

    @Test
    fun `get suggestions should update UI state correctly`() = runTest {
        val suggestions = listOf("user3", "user4", "user5")

        coEvery { friendRepository.getSuggestions(0, 100, any()) } returns mockk {
            every { isSuccessful } returns true
            every { body() } returns mockk {
                every { data } returns suggestions
            }
        }

        viewModel.getSuggestions()
        advanceUntilIdle()

        assertEquals(suggestions, viewModel.uiState.value.suggestions)
    }
}