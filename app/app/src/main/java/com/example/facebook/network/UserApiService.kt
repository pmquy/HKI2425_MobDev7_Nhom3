package com.example.facebook.network

import com.example.facebook.model.LoginRequest
import com.example.facebook.model.OtpRequest
import com.example.facebook.model.RegisterRequest
import com.example.facebook.model.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query


interface UserApiService {
    @POST("api/v1/user/login")
    suspend fun login(
        @Body loginRequest: LoginRequest
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

    @POST("api/v1/user")
    suspend fun signUp(@Body signUpRequest: RegisterRequest): Response<User>

    @POST("api/v1/user/otp")
    suspend fun verifyOtp(@Body otpRequest: OtpRequest): Response<User>
}