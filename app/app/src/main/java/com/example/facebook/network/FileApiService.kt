package com.example.facebook.network

import com.example.facebook.model.File
import com.example.facebook.model.GetSystemFileResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface FileApiService {
    @GET("api/v1/file/{id}")
    suspend fun getById(@Path("id") id: String): Response<File>

    @GET("api/v1/file/system")
    suspend fun getSystemFile(
        @Query("type") type: String,
        @Query("offset") offset: Int?,
        @Query("limit") limit: Int?,
    ): Response<GetSystemFileResponse>
}