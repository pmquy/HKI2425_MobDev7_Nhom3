package com.example.facebook.ui.components

import com.example.facebook.data.FileRepository
import com.example.facebook.model.File
import com.example.facebook.model.GetSystemFileResponse
import retrofit2.Response

class fakeFileRepository : FileRepository {
    val mockFile0 = File(
        _id = "0",
        url = "https://example.com/0",
        blurUrl = "https://example.com/0/blur",
        type = "voice",
        status = "active",
        name = "file0",
        description = "file0 description",
        createdAt = "2022-01-01T00:00:00.000Z",
        updatedAt = "2022-01-01T00:00:00.000Z",
    )
    val mockFile1 = File(
        _id = "1",
        url = "https://example.com/1",
        blurUrl = "https://example.com/1/blur",
        type = "video",
        status = "active",
        name = "file1",
        description = "file1 description",
        createdAt = "2022-01-01T00:00:00.000Z",
        updatedAt = "2022-01-01T00:00:00.000Z",
    )
    val mockFile2 = File(
        _id = "2",
        url = "https://example.com/2",
        blurUrl = "https://example.com/2/blur",
        type = "video",
        status = "active",
        name = "file2",
        description = "file2 description",
        createdAt = "2022-01-01T00:00:00.000Z",
        updatedAt = "2022-01-01T00:00:00.000Z",
    )
    val mockFiles = mutableListOf(mockFile0, mockFile1, mockFile2)

    override suspend fun getById(id: String): Response<File> {
        return when (id) {
            "0" -> Response.success(mockFile0)
            "1" -> Response.success(mockFile1)
            "2" -> Response.success(mockFile2)
            else -> Response.error(404, null)
        }
    }

    override suspend fun getSystemFile(
        type: String,
        offset: Int?,
        limit: Int?
    ): Response<GetSystemFileResponse> {
        return Response.success(GetSystemFileResponse(true, mockFiles))
    }
}