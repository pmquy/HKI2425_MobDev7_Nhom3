package com.example.facebook.network

import com.example.facebook.model.GetUsersResponse
import com.example.facebook.model.LoginRequest
import com.example.facebook.model.OtpRequest
import com.example.facebook.model.User
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query


interface UserApiService {
    @POST("api/v1/user/login")
    suspend fun login(
        @Body loginRequest: LoginRequest,
        @Query("socketId") socketId: String? = null,
        @Query("token") token: String? = null,
    ): Response<User>

    @GET("api/v1/user/auth")
    suspend fun auth(
        @Query("token") token: String?,
        @Query("socketId") socketId: String?
    ): Response<User>

    @GET("api/v1/user/{id}")
    suspend fun getById(
        @Path("id") id: String
    ): Response<User>

    @Multipart
    @POST("api/v1/user")
    suspend fun register(
        @Part("firstName") firstName: RequestBody,
        @Part("lastName") lastName: RequestBody,
        @Part("email") email: RequestBody,
        @Part("password") password: RequestBody,
        @Part("phoneNumber") phoneNumber: RequestBody,
        @Part avatar: MultipartBody.Part?
    ): Response<User>

    @POST("api/v1/user/otp")
    suspend fun verifyOtp(@Body otpRequest: OtpRequest): Response<User>

    @Multipart
    @PUT("api/v1/user/")
    suspend fun update(
        @Part("firstName") firstName: RequestBody?,
        @Part("lastName") lastName: RequestBody?,
        @Part("password") password: RequestBody?,
        @Part("phoneNumber") phoneNumber: RequestBody?,
        @Part avatar: MultipartBody.Part?
    ): Response<User>

    @GET("api/v1/user")
    suspend fun getUsers(
        @Query("offset") offset: Int,
        @Query("limit") limit: Int,
        @Query("q") q: String
    ) : Response<GetUsersResponse>

    @POST("api/v1/user/logout")
    suspend fun logout(): Response<Void>
}