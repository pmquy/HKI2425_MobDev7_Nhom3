package com.example.facebook.data

import com.example.facebook.model.LoginRequest
import com.example.facebook.model.User
import com.example.facebook.network.UserApiService
import retrofit2.Response

interface UserRepository {
    suspend fun login(email: String, password: String): Response<User>
    suspend fun auth(token: String?, socketId: String?): Response<User>
    suspend fun getById(id: String): Response<User>
}

class NetworkUserRepository(
    private val userApiService: UserApiService
) : UserRepository {

    override suspend fun login(email: String, password: String): Response<User> =
        userApiService.login(LoginRequest(email = email, password = password))

    override suspend fun auth(token: String?, socketId: String?): Response<User> =
        userApiService.auth(token, socketId)

    override suspend fun getById(id: String): Response<User> = userApiService.getById(id)
}