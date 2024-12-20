package com.example.facebook.ui.screens

import com.example.facebook.FacebookApplication
import com.example.facebook.data.SocketRepository
import com.example.facebook.data.UserPreferenceRepository
import com.example.facebook.data.UserRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private lateinit var viewModel: UserViewModel
    private val userRepository = mockk<UserRepository>()
    private val socketRepository = mockk<SocketRepository>(relaxed = true)
    private val userPreferenceRepository = mockk<UserPreferenceRepository>()
    private val application = mockk<FacebookApplication>(relaxed = true)

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        every { application.user } returns mockk {
            every { _id } returns "mock_user_id"
        }

        viewModel = UserViewModel(
            userRepository = userRepository,
            socketRepository = socketRepository,
            userPreferenceRepository = userPreferenceRepository,
            application = application
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }


    @Test(expected = Exception::class)
    fun `login with incorrect credentials should throw exception`() = runTest {
        val email = "wrong@example.com"
        val password = "wrongPassword"

        coEvery {
            userRepository.login(
                email,
                password,
                any(),
                any()
            )
        } throws Exception("Authentication failed")

        viewModel.login(email, password)
    }

    @Test
    fun `checkIfUser should return true for current user id`() {
        assertTrue(viewModel.checkIfUser("mock_user_id"))
    }

    @Test
    fun `checkIfUser should return false for a different user id`() {
        assertFalse(viewModel.checkIfUser("different_user_id"))
    }

    @Test(expected = Exception::class)
    fun `auth with invalid token should throw exception`() = runTest {
        coEvery { userRepository.auth(any(), any()) } throws Exception("Authorization failed")

        viewModel.auth()
    }

    @Test(expected = Exception::class)
    fun `handleUpdate with error response should throw exception`() = runTest {

        coEvery {
            userRepository.update(
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } throws Exception("Update failed")

        viewModel.handleUpdate(firstName = "Caio")
    }

}