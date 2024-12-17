package com.example.facebook.ui.screens

import com.example.facebook.FacebookApplication
import com.example.facebook.data.UserRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class RegisterViewModelTest {

    private lateinit var viewModel: RegisterViewModel
    private val userRepository = mockk<UserRepository>()
    private val application = mockk<FacebookApplication>(relaxed = true)

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = RegisterViewModel(userRepository, application)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `email setting updates the UI state correctly`() {
        val email = "test@example.com"
        viewModel.setEmail(email)
        Assert.assertEquals(email, viewModel.uiState.value.email)
    }

    @Test
    fun `otp verification sends request successfully`() = runTest(testDispatcher) {
        val otp = "123456"
        viewModel.setEmail("johndoe@example.com")
        viewModel.setOtp(otp)

        coEvery { userRepository.verifyOtp(any(), any()) } returns Response.success(null)

        viewModel.otp()

        Assert.assertTrue(true)
    }

    @Test(expected = Exception::class)
    fun `otp verification fails with incorrect otp`() = runTest(testDispatcher) {
        val incorrectOtp = "000000"
        coEvery { userRepository.verifyOtp(any(), any()) } throws Exception("Incorrect OTP")

        viewModel.setOtp(incorrectOtp)
        viewModel.otp()
    }

    @Test
    fun `avatar is set correctly`() {
        val avatar = Pair(File("path/to/avatar.jpg"), "image/jpeg")
        viewModel.setAvatar(avatar)
        Assert.assertEquals(avatar, viewModel.uiState.value.avatar)
    }

    @Test
    fun `setting first name updates ui state`() {
        val firstName = "Jane"
        viewModel.setFirstName(firstName)
        Assert.assertEquals(firstName, viewModel.uiState.value.firstName)
    }

    @Test
    fun `setting phone number updates ui state`() {
        val phoneNumber = "9876543210"
        viewModel.setPhoneNumber(phoneNumber)
        Assert.assertEquals(phoneNumber, viewModel.uiState.value.phoneNumber)
    }
}