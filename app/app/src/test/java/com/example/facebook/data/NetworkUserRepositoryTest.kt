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
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.io.File

class NetworkUserRepositoryTest {

    private lateinit var repository: NetworkUserRepository
    private val userApiService = mockk<UserApiService>()

    @Before
    fun setup() {
        repository = NetworkUserRepository(userApiService)
    }

    @Test
    fun `login should return User object for valid credentials`() = runTest {
        val email = "test@example.com"
        val password = "password123"
        val token = "testToken"
        val socketId = "testSocketId"
        val mockUser = mockk<User>()
        val mockResponse = mockk<Response<User>> {
            every { isSuccessful } returns true
            every { body() } returns mockUser
        }

        coEvery {
            userApiService.login(
                LoginRequest(email = email, password = password),
                token = token,
                socketId = socketId
            )
        } returns mockResponse

        val result = repository.login(email, password, token, socketId)

        assertTrue(result.isSuccessful)
        assertEquals(mockUser, result.body())
    }

    @Test
    fun `login should return error response when credentials are invalid`() = runTest {
        // Arrange
        val email = "invalid@example.com"
        val password = "wrongpassword"
        val token = null
        val socketId = null
        val errorResponse = Response.error<User>(401, "Unauthorized".toResponseBody(null))

        coEvery { userApiService.login(any(), any(), any()) } returns errorResponse

        // Act
        val result = repository.login(email, password, token, socketId)

        // Assert
        assertFalse(result.isSuccessful)
        assertEquals(401, result.code())
    }

    @Test
    fun `auth should return user response for valid token and socketId`() = runTest {
        val mockUserApiService = mockk<UserApiService>()
        val repository = NetworkUserRepository(mockUserApiService)
        val token = "validToken"
        val socketId = "validSocketId"
        val mockUser = mockk<User>()
        val mockResponse = mockk<Response<User>> {
            every { isSuccessful } returns true
            every { body() } returns mockUser
        }

        coEvery { mockUserApiService.auth(token, socketId) } returns mockResponse

        val result = repository.auth(token, socketId)

        coVerify { mockUserApiService.auth(token, socketId) }
        assertTrue(result.isSuccessful)
        assertEquals(mockUser, result.body())
    }

    @Test
    fun `getById should retrieve user by ID and return correct User object`() = runTest {
        val userId = "testUserId"
        val expectedUser = User(_id = userId, firstName = "John", lastName = "Doe")
        val mockResponse: Response<User> = mockk {
            every { isSuccessful } returns true
            every { body() } returns expectedUser
        }

        coEvery { userApiService.getById(userId) } returns mockResponse

        val repository = NetworkUserRepository(userApiService)
        val result = repository.getById(userId)

        assertTrue(result.isSuccessful)
        assertEquals(expectedUser, result.body())
        coVerify { userApiService.getById(userId) }
    }

    @Test
    fun `register should create a new user with all required fields including avatar`() = runTest {
        val userApiService = mockk<UserApiService>()
        val networkUserRepository = NetworkUserRepository(userApiService)

        val firstName = "John"
        val lastName = "Doe"
        val email = "john.doe@example.com"
        val password = "password123"
        val phoneNumber = "1234567890"
        val avatarFile = File("path/to/avatar.jpg")
        val avatarMimeType = "image/jpeg"
        val avatar = Pair(avatarFile, avatarMimeType)

        val mockUser = mockk<User>()
        val mockResponse = mockk<Response<User>> {
            every { isSuccessful } returns true
            every { body() } returns mockUser
        }

        coEvery {
            userApiService.register(
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns mockResponse

        val result = networkUserRepository.register(
            firstName,
            lastName,
            email,
            password,
            phoneNumber,
            avatar
        )

        coVerify {
            userApiService.register(
                firstName = any(),
                lastName = any(),
                email = any(),
                password = any(),
                phoneNumber = any(),
                avatar = any()
            )
        }

        assertEquals(mockResponse, result)

        // Additional verifications for the content of the arguments
        val firstNameSlot = slot<RequestBody>()
        val lastNameSlot = slot<RequestBody>()
        val emailSlot = slot<RequestBody>()
        val passwordSlot = slot<RequestBody>()
        val phoneNumberSlot = slot<RequestBody>()
        val avatarSlot = slot<MultipartBody.Part>()

        coVerify {
            userApiService.register(
                capture(firstNameSlot),
                capture(lastNameSlot),
                capture(emailSlot),
                capture(passwordSlot),
                capture(phoneNumberSlot),
                capture(avatarSlot)
            )
        }

        assertTrue(
            firstNameSlot.captured.contentType()?.toString()?.startsWith("text/plain") == true
        )
        assertEquals(firstName.length.toLong(), firstNameSlot.captured.contentLength())

        assertTrue(
            lastNameSlot.captured.contentType()?.toString()?.startsWith("text/plain") == true
        )
        assertEquals(lastName.length.toLong(), lastNameSlot.captured.contentLength())

        assertTrue(emailSlot.captured.contentType()?.toString()?.startsWith("text/plain") == true)
        assertEquals(email.length.toLong(), emailSlot.captured.contentLength())

        assertTrue(
            passwordSlot.captured.contentType()?.toString()?.startsWith("text/plain") == true
        )
        assertEquals(password.length.toLong(), passwordSlot.captured.contentLength())

        assertTrue(
            phoneNumberSlot.captured.contentType()?.toString()?.startsWith("text/plain") == true
        )
        assertEquals(phoneNumber.length.toLong(), phoneNumberSlot.captured.contentLength())

        assertEquals(avatarMimeType, avatarSlot.captured.body.contentType()?.toString())
        assertTrue(
            avatarSlot.captured.headers?.get("Content-Disposition")
                ?.contains("filename=\"avatar.jpg\"") == true
        )
    }

    @Test
    fun `update should partially update user information`() = runTest {
        val userApiService = mockk<UserApiService>()
        val repository = NetworkUserRepository(userApiService)
        val updatedUser = User(_id = "1", firstName = "John", lastName = "Doe")

        coEvery {
            userApiService.update(
                firstName = any(),
                lastName = any(),
                password = null,
                phoneNumber = null,
                avatar = null
            )
        } returns Response.success(updatedUser)

        val result = repository.update(
            firstName = "John",
            lastName = "Doe",
            password = null,
            phoneNumber = null,
            avatar = null
        )

        assertTrue(result.isSuccessful)
        assertEquals(updatedUser, result.body())
        coVerify {
            userApiService.update(
                firstName = any(),
                lastName = any(),
                password = null,
                phoneNumber = null,
                avatar = null
            )
        }
    }

    @Test
    fun `logout should call userApiService logout`() = runTest {
        val userApiService = mockk<UserApiService>()
        val repository = NetworkUserRepository(userApiService)

        coEvery { userApiService.logout() } returns Response.success(null)

        val result = repository.logout()

        coVerify(exactly = 1) { userApiService.logout() }
        assertTrue(result.isSuccessful)
    }

    @Test
    fun `getUsers should retrieve paginated list of users`() = runTest {
        val offset = 0
        val limit = 10
        val query = "test"
        val mockResponse = mockk<Response<GetUsersResponse>>()
        val mockGetUsersResponse = mockk<GetUsersResponse>()

        coEvery { userApiService.getUsers(offset, limit, query) } returns mockResponse
        every { mockResponse.isSuccessful } returns true
        every { mockResponse.body() } returns mockGetUsersResponse

        val result = repository.getUsers(offset, limit, query)

        coVerify { userApiService.getUsers(offset, limit, query) }
        assertEquals(mockResponse, result)
    }

    @Test
    fun `verifyOtp should call userApiService verifyOtp with correct parameters and return response`() =
        runTest {
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

            coVerify { userApiService.verifyOtp(OtpRequest(email = email, otp = otp)) }
            assertEquals(mockResponse, result)
            assertTrue(result.isSuccessful)
            assertEquals(mockUser, result.body())
        }

    @Test
    fun `all API calls should handle network errors gracefully`() = runTest {
        val errorResponseUser = Response.error<User>(500, "".toResponseBody(null))
        val errorResponseVoid = Response.error<Void>(500, "".toResponseBody(null))
        val errorResponseGetUsers =
            Response.error<GetUsersResponse>(500, "".toResponseBody(null))

        coEvery { userApiService.login(any(), any(), any()) } returns errorResponseUser
        coEvery { userApiService.auth(any(), any()) } returns errorResponseUser
        coEvery { userApiService.getById(any()) } returns errorResponseUser
        coEvery {
            userApiService.register(
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns errorResponseUser
        coEvery {
            userApiService.update(
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns errorResponseUser
        coEvery { userApiService.logout() } returns errorResponseVoid
        coEvery { userApiService.getUsers(any(), any(), any()) } returns errorResponseGetUsers
        coEvery { userApiService.verifyOtp(any()) } returns errorResponseUser

        val loginResult = repository.login("email", "password", null, null)
        val authResult = repository.auth(null, null)
        val getByIdResult = repository.getById("id")
        val registerResult =
            repository.register("first", "last", "email", "password", "phone", null)
        val updateResult = repository.update(null, null, null, null, null)
        val logoutResult = repository.logout()
        val getUsersResult = repository.getUsers(0, 10, "")
        val verifyOtpResult = repository.verifyOtp("email", "otp")

        assertFalse(loginResult.isSuccessful)
        assertFalse(authResult.isSuccessful)
        assertFalse(getByIdResult.isSuccessful)
        assertFalse(registerResult.isSuccessful)
        assertFalse(updateResult.isSuccessful)
        assertFalse(logoutResult.isSuccessful)
        assertFalse(getUsersResult.isSuccessful)
        assertFalse(verifyOtpResult.isSuccessful)
    }
}
