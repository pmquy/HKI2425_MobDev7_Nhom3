package com.example.facebook.ui.screens

import com.example.facebook.network.UserApiService
import com.example.facebook.data.NetworkUserRepository
import com.example.facebook.data.UserRepository
import com.example.facebook.model.GetUsersResponse
import com.example.facebook.model.User
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.io.File

class test {

    private lateinit var userApiService: UserApiService
    private lateinit var userRepository: NetworkUserRepository

    @Before
    fun setup() {
        // Tạo một mock của UserApiService
        userApiService = mockk()

        // Khởi tạo NetworkUserRepository với mock UserApiService
        userRepository = NetworkUserRepository(userApiService)

        // Tạo một đối tượng User mẫu
        val sampleUser = User(
            _id = "1",
            firstName = "John",
            lastName = "Doe",
            email = "john.doe@example.com",
            phoneNumber = "1234567890"
            // Thêm các trường khác nếu cần
        )

        // Thiết lập behavior cho mock UserApiService
        io.mockk.coEvery { userApiService.getById("1") } returns Response.success(sampleUser)
    }

    @Test
    fun testGetUserById() = runBlocking {
        val result = userRepository.getById("1")
        assert(result.isSuccessful)
        assert(result.body()?.firstName == "John")
        assert(result.body()?.lastName == "Doe")
    }

    // Thêm các test case khác ở đây
}
