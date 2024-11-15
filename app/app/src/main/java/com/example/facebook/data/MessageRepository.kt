package com.example.facebook.data

import com.example.facebook.model.Message
import com.example.facebook.network.MessageApiService
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.File

interface MessageRepository {
    suspend fun getById(id: String): Response<Message>
    suspend fun create(message: String, chatgroup: String, files: List<File>): Response<Message>
    suspend fun updateById(id: String, message: Message): Response<Message>
    suspend fun deleteById(id: String): Response<Void>
    suspend fun getAll(): Response<List<Message>>
}


class NetworkMessageRepository(
    private val messageApiService: MessageApiService
) : MessageRepository {
    override suspend fun getById(id: String): Response<Message> = messageApiService.getById(id)
    override suspend fun create(
        message: String,
        chatgroup: String,
        files: List<File>
    ): Response<Message> {
        return messageApiService.create(
            chatgroup.toRequestBody("text/plain".toMediaType()),
            message.toRequestBody("text/plain".toMediaType()),
            files.map { file ->
                val requestFile = file.asRequestBody("image/*".toMediaType())
                MultipartBody.Part.createFormData("files", file.name, requestFile)
            }
        )
    }

    override suspend fun updateById(id: String, message: Message): Response<Message> = messageApiService.updateById(id, message)
    override suspend fun deleteById(id: String): Response<Void> = messageApiService.deleteById(id)
    override suspend fun getAll(): Response<List<Message>> = messageApiService.getAll()
}