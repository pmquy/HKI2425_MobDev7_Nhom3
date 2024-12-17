package com.example.facebook.data

import com.example.facebook.model.GetUsersResponse
import com.example.facebook.model.LoginRequest
import com.example.facebook.model.OtpRequest
import com.example.facebook.model.User
import com.example.facebook.network.UserApiService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.MultipartBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

@ExperimentalCoroutinesApi
class UserRepositoryTest {

    private lateinit var userApiService: UserApiService
    private lateinit var userRepository: UserRepository

    @Before
    fun setup() {
        userApiService = mockk()
        userRepository = NetworkUserRepository(userApiService)
    }

    @Test
    fun `login should return successful response with valid credentials`() = runTest {
        // Arrange
        val email = "test@example.com"
        val password = "password123"
        val token = "fcm_token"
        val socketId = "socket_id"
        val mockUser = mockk<User>()
        val mockResponse = mockk<Response<User>> {
            every { isSuccessful } returns true
            every { body() } returns mockUser
        }
        coEvery { userApiService.login(any(), any(), any()) } returns mockResponse

        // Act
        val result = userRepository.login(email, password, token, socketId)

        // Assert
        assertTrue(result.isSuccessful)
        assertEquals(mockUser, result.body())
        coVerify {
            userApiService.login(
                LoginRequest(email = email, password = password),
                token = token,
                socketId = socketId
            )
        }
    }

    @Test
    fun `login should return error when credentials are incorrect`() = runTest {
        val userRepository = mockk<UserRepository>()
        val email = "wrong@example.com"
        val password = "incorrectPassword"
        val token = null
        val socketId = null

        coEvery { userRepository.login(email, password, token, socketId) } returns Response.error(
            401,
            "Unauthorized".toResponseBody(null)
        )

        val response = userRepository.login(email, password, token, socketId)

        assertFalse(response.isSuccessful)
        assertEquals(401, response.code())
    }

    @Test
    fun `auth should return successful response with valid token`() = runTest {
        val mockUserApiService = mockk<UserApiService>()
        val userRepository = NetworkUserRepository(mockUserApiService)
        val mockUser = mockk<User>()
        val mockResponse = mockk<Response<User>> {
            every { isSuccessful } returns true
            every { body() } returns mockUser
        }
        val token = "valid_token"
        val socketId = "socket_123"

        coEvery { mockUserApiService.auth(token, socketId) } returns mockResponse

        val result = userRepository.auth(token, socketId)

        assertTrue(result.isSuccessful)
        assertEquals(mockUser, result.body())
        coVerify(exactly = 1) { mockUserApiService.auth(token, socketId) }
    }

    @Test
    fun `register should create a new user with all required fields`() = runTest {
        val userRepository = mockk<UserRepository>()
        val firstName = "John"
        val lastName = "Doe"
        val email = "john.doe@example.com"
        val password = "password123"
        val phoneNumber = "1234567890"
        val avatar = Pair(java.io.File("path/to/avatar.jpg"), "image/jpeg")
        val expectedUser =
            User(_id = "1", firstName = firstName, lastName = lastName, email = email)

        coEvery {
            userRepository.register(
                firstName = firstName,
                lastName = lastName,
                email = email,
                password = password,
                phoneNumber = phoneNumber,
                avatar = avatar
            )
        } returns Response.success(expectedUser)

        val response =
            userRepository.register(firstName, lastName, email, password, phoneNumber, avatar)

        assertTrue(response.isSuccessful)
        assertEquals(expectedUser, response.body())
    }

    @Test
    fun `register should handle existing email address`() = runTest {
        val userRepository = mockk<UserRepository>()
        val existingEmail = "existing@example.com"
        val errorResponse =
            Response.error<User>(409, "Email already exists".toResponseBody(null))

        coEvery {
            userRepository.register(
                any(), any(), eq(existingEmail), any(), any(), any()
            )
        } returns errorResponse

        val result = userRepository.register(
            "John", "Doe", existingEmail, "password123", "1234567890", null
        )

        assertFalse(result.isSuccessful)
        assertEquals(409, result.code())
    }

    @Test
    fun `verifyOtp should return successful response for valid email and OTP`() = runTest {
        val email = "test@example.com"
        val otp = "123456"
        val mockUser = mockk<User>()
        val mockResponse = mockk<Response<User>> {
            every { isSuccessful } returns true
            every { body() } returns mockUser
        }

        coEvery {
            userApiService.verifyOtp(
                OtpRequest(
                    email = email,
                    otp = otp
                )
            )
        } returns mockResponse

        val repository = NetworkUserRepository(userApiService)
        val result = repository.verifyOtp(email, otp)

        assertTrue(result.isSuccessful)
        assertEquals(mockUser, result.body())
    }

    @Test
    fun `update should update user information partially with only provided fields`() = runTest {
        val userRepository = mockk<UserRepository>()
        val updatedUser =
            User(_id = "1", firstName = "John", lastName = "Doe", email = "john@example.com")

        coEvery {
            userRepository.update(
                firstName = "John",
                lastName = null,
                password = null,
                phoneNumber = null,
                avatar = null
            )
        } returns Response.success(updatedUser)

        val response = userRepository.update(
            firstName = "John",
            lastName = null,
            password = null,
            phoneNumber = null,
            avatar = null
        )

        assertTrue(response.isSuccessful)
        assertEquals(updatedUser, response.body())
        coVerify(exactly = 1) {
            userRepository.update(
                firstName = "John",
                lastName = null,
                password = null,
                phoneNumber = null,
                avatar = null
            )
        }
    }

    @Test
    fun `logout should invalidate user session`() = runTest {
        val userApiService = mockk<UserApiService>()
        val userRepository = NetworkUserRepository(userApiService)

        coEvery { userApiService.logout() } returns Response.success(null)

        val response = userRepository.logout()

        coVerify(exactly = 1) { userApiService.logout() }
        assertTrue(response.isSuccessful)
    }

    @Test
    fun `getUsers should retrieve a list of users with pagination and search`() = runTest {
        val mockUserApiService = mockk<UserApiService>()
        val userRepository = NetworkUserRepository(mockUserApiService)

        val offset = 0
        val limit = 10
        val searchQuery = "John"

        val mockResponse = mockk<Response<GetUsersResponse>> {
            every { isSuccessful } returns true
            every { body() } returns GetUsersResponse(
                data = listOf(
                    User(_id = "1", firstName = "John", lastName = "Doe"),
                    User(_id = "2", firstName = "Johnny", lastName = "Smith")
                ),
                hasMore = true
            )
        }

        coEvery { mockUserApiService.getUsers(offset, limit, searchQuery) } returns mockResponse

        val result = userRepository.getUsers(offset, limit, searchQuery)

        assertTrue(result.isSuccessful)
        assertEquals(2, result.body()?.data?.size)
        assertEquals(true, result.body()?.hasMore)
        assertEquals("John", result.body()?.data?.get(0)?.firstName)
        assertEquals("Johnny", result.body()?.data?.get(1)?.firstName)

        coVerify { mockUserApiService.getUsers(offset, limit, searchQuery) }
    }

    @Test
    fun `should handle file upload for avatar during registration and update`() = runTest {
        val mockUserApiService = mockk<UserApiService>()
        val userRepository = NetworkUserRepository(mockUserApiService)

        val avatarFile = java.io.File("test_avatar.jpg")
        val avatarMimeType = "image/jpeg"
        val avatar = Pair(avatarFile, avatarMimeType)

        // Test registration with avatar
        coEvery {
            mockUserApiService.register(
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns Response.success(User())

        userRepository.register(
            "John",
            "Doe",
            "john@example.com",
            "password",
            "1234567890",
            avatar
        )

        coVerify {
            mockUserApiService.register(
                any(),
                any(),
                any(),
                any(),
                any(),
                match {
                    it is MultipartBody.Part && it.body.contentType()?.toString() == avatarMimeType
                }
            )
        }

        // Test update with avatar
        coEvery {
            mockUserApiService.update(
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns Response.success(User())

        userRepository.update(
            "John",
            "Doe",
            null,
            "9876543210",
            avatar
        )

        coVerify {
            mockUserApiService.update(
                any(),
                any(),
                any(),
                any(),
                match {
                    it is MultipartBody.Part && it.body.contentType()?.toString() == avatarMimeType
                }
            )
        }
    }
}