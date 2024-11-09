package com.example.facebook.network

import com.example.facebook.model.File
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface FileApiService {
    @GET("api/v1/file/{id}")
    suspend fun getById(@Path("id") id: String): Response<File>
}