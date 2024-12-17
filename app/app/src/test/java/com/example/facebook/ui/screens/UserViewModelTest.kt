package com.example.facebook.ui.screens

import com.example.facebook.FacebookApplication
import com.example.facebook.data.SocketRepository
import com.example.facebook.data.UserPreferenceRepository
import com.example.facebook.data.UserRepository
import com.example.facebook.model.User
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response


@OptIn(ExperimentalCoroutinesApi::class)
class UserViewModelTest {

    private lateinit var viewModel: UserViewModel
    private val userRepository = mockk<UserRepository>()
    private val socketRepository = mockk<SocketRepository>()
    private val userPreferenceRepository = mockk<UserPreferenceRepository>()
    private val application = mockk<FacebookApplication>(relaxed = true)

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel =
            UserViewModel(userRepository, socketRepository, userPreferenceRepository, application)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `getUserById returns correct user`() = runTest {
        val mockUser = User(
            _id = "123",
            firstName = "John",
            lastName = "Doe",
            phoneNumber = "",
            email = "",
            avatar = "",
            createdAt = "",
            updatedAt = ""
        )
        coEvery { userRepository.getById("123") } returns Response.success(mockUser)

        val userFlow = viewModel.getUserById("123")

        // Triggering the dispatcher to run coroutines
        advanceUntilIdle() // Makes sure all coroutines finish
        runCurrent() // Runs the current queued coroutines immediately

        assertEquals(mockUser, userFlow.value)
    }


    @Test
    fun `login failure throws exception`() = runTest {
        coEvery { userRepository.login(any(), any(), any(), any()) } returns Response.error(
            400,
            mockk(relaxed = true)
        )

        assertThrows(Exception::class.java) {
            runTest { viewModel.login("email", "password") }
        }
    }

    @Test
    fun `auth failure throws exception`() = runTest {
        coEvery { userRepository.auth(any(), any()) } returns Response.error(
            400,
            mockk(relaxed = true)
        )

        assertThrows(Exception::class.java) {
            runTest { viewModel.auth() }
        }
    }

    @Test
    fun `handleUpdate success updates user state`() = runTest {
        val updatedUser = User(_id = "123", firstName = "Jane", lastName = "Doe")
        coEvery {
            userRepository.update(
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns Response.success(updatedUser)

        viewModel.handleUpdate(firstName = "Jane")
        runCurrent()

        assertEquals(updatedUser, viewModel.uiState.value.user)
    }

    @Test
    fun `logout success call user repository's logout`() = runTest {
        coEvery { userRepository.logout() } returns Response.success(null)

        viewModel.logout()
        runCurrent()

        coVerify { userRepository.logout() }
    }

    @Test
    fun `logout failure throws exception`() = runTest {
        coEvery { userRepository.logout() } returns Response.error(400, mockk(relaxed = true))

        assertThrows(Exception::class.java) {
            runTest { viewModel.logout() }
        }
    }

    @Test
    fun `checkIfUser returns true for matching user id`() {
        val mockUser = User(_id = "123", firstName = "John", lastName = "Doe")
        every { application.user } returns mockUser

        val result = viewModel.checkIfUser("123")

        assertTrue(result)
    }

    @Test
    fun `checkIfUser returns false for non-matching user id`() {
        val mockUser = User(_id = "456", firstName = "John", lastName = "Doe")
        every { application.user } returns mockUser

        val result = viewModel.checkIfUser("123")

        assertFalse(result)
    }
}