package com.example.facebook.network

import com.example.facebook.model.Message
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.Path

interface MessageApiService {
    @GET("api/v1/message/{id}")
    suspend fun getById(@Path("id") id: String): Response<Message>

    @Multipart
    @POST("api/v1/message")
    suspend fun create(
        @Part("chatgroup") chatgroup: RequestBody,
        @Part("message") message: RequestBody,
        @PartMap systemFiles: HashMap<String, RequestBody>,
        @Part files: List<MultipartBody.Part>
    ): Response<Message>

    @PUT("api/v1/message/{id}")
    suspend fun updateById(@Path("id") id: String, @Body message: Message): Response<Message>

    @DELETE("api/v1/message/{id}")
    suspend fun deleteById(@Path("id") id: String): Response<Void>

    @GET("api/v1/message")
    suspend fun getAll(): Response<List<Message>>
}