package com.example.facebook.data

import com.example.facebook.model.File
import com.example.facebook.network.FileApiService
import retrofit2.Response

interface FileRepository {
    suspend fun getById(id: String): Response<File>
}

class NetworkFileRepository(
    private val fileApiService: FileApiService
) : FileRepository {

    override suspend fun getById(id: String): Response<File> = fileApiService.getById(id)
}


