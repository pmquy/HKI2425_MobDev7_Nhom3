package com.example.facebook.data

import com.example.facebook.model.GetUsersResponse
import com.example.facebook.model.LoginRequest
import com.example.facebook.model.OtpRequest
import com.example.facebook.model.User
import com.example.facebook.network.UserApiService
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.File

interface UserRepository {
    suspend fun login(
        email: String, password: String, socketId: String?, token: String?
    ): Response<User>

    suspend fun auth(token: String?, socketId: String?): Response<User>
    suspend fun getById(id: String): Response<User>
    suspend fun register(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        phoneNumber: String,
        avatar: Pair<File, String>
    ): Response<User>

    suspend fun update(
        firstName: String?,
        lastName: String?,
        password: String?,
        phoneNumber: String?,
        avatar: Pair<File, String>?
    ): Response<User>

    suspend fun otp(email: String, otp: String): Response<Void>
    suspend fun logout(): Response<Void>

    suspend fun getUsers(offset: Int, limit: Int, q: String): Response<GetUsersResponse>
}

class NetworkUserRepository(
    private val userApiService: UserApiService
) : UserRepository {

    override suspend fun login(
        email: String, password: String, socketId: String?, token: String?
    ) = userApiService.login(LoginRequest(email = email, password = password), socketId, token)

    override suspend fun auth(token: String?, socketId: String?) =
        userApiService.auth(token, socketId)

    override suspend fun getById(id: String) = userApiService.getById(id)

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

    override suspend fun update(
        firstName: String?,
        lastName: String?,
        password: String?,
        phoneNumber: String?,
        avatar: Pair<File, String>?,
    ): Response<User> = userApiService.update(
        firstName?.toRequestBody("text/plain".toMediaType()),
        lastName?.toRequestBody("text/plain".toMediaType()),
        password?.toRequestBody("text/plain".toMediaType()),
        phoneNumber?.toRequestBody("text/plain".toMediaType()),
        avatar?.let {
            MultipartBody.Part.createFormData(
                "avatar", it.first.name, it.first.asRequestBody(it.second.toMediaType())
            )
        }
    )

    override suspend fun otp(email: String, otp: String) =
        userApiService.otp(OtpRequest(email = email, otp = otp))

    override suspend fun logout() = userApiService.logout()

    override suspend fun getUsers(
        offset: Int,
        limit: Int,
        q: String,
    ): Response<GetUsersResponse> = userApiService.getUsers(offset, limit, q)
}
