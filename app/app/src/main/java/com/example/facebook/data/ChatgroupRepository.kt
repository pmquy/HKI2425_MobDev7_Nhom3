package com.example.facebook.data

import com.example.facebook.model.ChatGroup
import com.example.facebook.model.GetAllChatGroupsResponse
import com.example.facebook.model.GetMessagesResponse
import com.example.facebook.model.Member
import com.example.facebook.network.ChatgroupApiService
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.File

interface ChatGroupRepository {
    suspend fun getById(id: String): Response<ChatGroup>
    suspend fun create(
        name: String,
        users: List<Member>,
        avatar: Pair<File, String>?
    ): Response<ChatGroup>

    suspend fun updateById(id: String, name: String, avatar: Pair<File, String>?): Response<ChatGroup>
    suspend fun deleteById(id: String): Response<Void>
    suspend fun getAll(offset: Int, limit: Int, query: String): Response<GetAllChatGroupsResponse>
    suspend fun getMessage(
        id: String,
        offset: Int,
        limit: Int,
        query: String
    ): Response<GetMessagesResponse>

    suspend fun addMember(id: String, members: List<Member>): Response<ChatGroup>
    suspend fun removeMember(id: String, member: Member): Response<ChatGroup>
    suspend fun updateMember(id: String, member: Member): Response<ChatGroup>
    suspend fun getMember(id: String): Response<List<Member>>
}

class NetworkChatGroupRepository(
    private val chatgroupApiService: ChatgroupApiService
) : ChatGroupRepository {
    override suspend fun getById(id: String): Response<ChatGroup> = chatgroupApiService.getById(id)
    override suspend fun create(
        name: String,
        users: List<Member>,
        avatar: Pair<File, String>?
    ): Response<ChatGroup> {

        val map = HashMap<String, RequestBody>()

        users.forEachIndexed { index, member ->
            map["users[$index][user]"] = member.user.toRequestBody("text/plain".toMediaType())
            map["users[$index][role]"] = member.role.toRequestBody("text/plain".toMediaType())
        }

        return chatgroupApiService.create(
            name.toRequestBody("text/plain".toMediaType()),
            map,
            avatar?.let {
                MultipartBody.Part.createFormData(
                    "avatar",
                    it.first.name,
                    it.first.asRequestBody(avatar.second.toMediaType())
                )
            }
        )
    }

    override suspend fun updateById(id: String, name: String, avatar: Pair<File, String>?): Response<ChatGroup> {
        val nameRequestBody = name.toRequestBody("text/plain".toMediaType())
        val avatarPart = avatar?.let {
            MultipartBody.Part.createFormData(
                "avatar", it.first.name, it.first.asRequestBody(it.second.toMediaType())
            )
        }
        val response = chatgroupApiService.updateById(id, nameRequestBody, avatarPart)
        if (!response.isSuccessful) {
            throw Exception("Error updating chat group: ${response.errorBody()?.string()}")
        }
        return response
    }
    override suspend fun deleteById(id: String): Response<Void> = chatgroupApiService.deleteById(id)
    override suspend fun getAll(
        offset: Int,
        limit: Int,
        query: String
    ): Response<GetAllChatGroupsResponse> = chatgroupApiService.getAll(offset, limit, query)

    override suspend fun getMessage(
        id: String,
        offset: Int,
        limit: Int,
        query: String
    ): Response<GetMessagesResponse> = chatgroupApiService.getMessage(id, offset, limit, query)

    override suspend fun addMember(id: String, members: List<Member>): Response<ChatGroup> =
        chatgroupApiService.addMembers(id, members)

    override suspend fun removeMember(id: String, member: Member): Response<ChatGroup> =
        chatgroupApiService.removeMember(id, member)

    override suspend fun updateMember(id: String, member: Member): Response<ChatGroup> =
        chatgroupApiService.updateMember(id, member)

    override suspend fun getMember(id: String): Response<List<Member>> =
        chatgroupApiService.getMember(id)

}