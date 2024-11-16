package com.example.facebook.data

import com.example.facebook.model.LoginRequest
import com.example.facebook.model.OtpRequest
import com.example.facebook.model.User
import com.example.facebook.network.UserApiService
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response
import java.io.File

interface UserRepository {
    suspend fun login(email: String, password: String): Response<User>
    suspend fun auth(token: String?, socketId: String?): Response<User>
    suspend fun getById(id: String): Response<User>
    suspend fun register(
        firstName: String,
        lastName: String ,
        email: String,
        password: String,
        phoneNumber: String,
        avatar: Pair<File, String>
    ): Response<User>
    suspend fun verifyOtp(email: String, otp: String): Response<User>
}

class NetworkUserRepository(
    private val userApiService: UserApiService
) : UserRepository {

    override suspend fun login(email: String, password: String): Response<User> =
        userApiService.login(LoginRequest(email = email, password = password))

    override suspend fun auth(token: String?, socketId: String?): Response<User> =
        userApiService.auth(token, socketId)

    override suspend fun getById(id: String): Response<User> = userApiService.getById(id)

    override suspend fun register(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        phoneNumber: String,
        avatar: Pair<File, String>,
    ): Response<User> = userApiService.register(
            firstName = firstName.toRequestBody("text/plain".toMediaType()),
            lastName = lastName.toRequestBody("text/plain".toMediaType()),
            email = email.toRequestBody("text/plain".toMediaType()),
            password = password.toRequestBody("text/plain".toMediaType()),
            phoneNumber = phoneNumber.toRequestBody("text/plain".toMediaType()),
            avatar = MultipartBody.Part.createFormData(
                "avatar", avatar.first.name, avatar.first.asRequestBody(avatar.second.toMediaType())
            )
        )

    override suspend fun verifyOtp(email: String, otp: String): Response<User> =
        userApiService.verifyOtp(OtpRequest(email = email, otp = otp))
}