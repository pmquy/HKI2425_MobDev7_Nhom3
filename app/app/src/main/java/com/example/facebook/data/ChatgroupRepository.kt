package com.example.facebook.data

import com.example.facebook.model.ChatGroup
import com.example.facebook.model.GetAllChatGroupsResponse
import com.example.facebook.model.GetMessagesResponse
import com.example.facebook.model.Member
import com.example.facebook.network.ChatgroupApiService
import retrofit2.Response

interface ChatGroupRepository {
    suspend fun getById(id: String): Response<ChatGroup>
    suspend fun create(chatGroup: ChatGroup): Response<ChatGroup>
    suspend fun updateById(id: String, chatGroup: ChatGroup): Response<ChatGroup>
    suspend fun deleteById(id: String): Response<Void>
    suspend fun getAll(page: Int, limit: Int, query: String): Response<GetAllChatGroupsResponse>
    suspend fun getMessage(id: String, page: Int, limit: Int, query: String): Response<GetMessagesResponse>
    suspend fun addMember(id: String, members: List<Member>): Response<ChatGroup>
    suspend fun removeMember(id: String, member: Member): Response<ChatGroup>
    suspend fun updateMember(id: String, member: Member): Response<ChatGroup>
    suspend fun getMember(id: String): Response<List<Member>>
}

class NetworkChatGroupRepository(
    private val chatgroupApiService: ChatgroupApiService
) : ChatGroupRepository {
    override suspend fun getById(id: String): Response<ChatGroup> = chatgroupApiService.getById(id)
    override suspend fun create(chatGroup: ChatGroup): Response<ChatGroup> = chatgroupApiService.create(chatGroup)
    override suspend fun updateById(id: String, chatGroup: ChatGroup): Response<ChatGroup> = chatgroupApiService.updateById(id, chatGroup)
    override suspend fun deleteById(id: String): Response<Void> = chatgroupApiService.deleteById(id)
    override suspend fun getAll(page: Int, limit: Int, query: String): Response<GetAllChatGroupsResponse> = chatgroupApiService.getAll(page, limit, query)
    override suspend fun getMessage(id: String, page: Int, limit: Int, query: String): Response<GetMessagesResponse> = chatgroupApiService.getMessage(id, page, limit, query)
    override suspend fun addMember(id: String, members: List<Member>): Response<ChatGroup> = chatgroupApiService.addMembers(id, members)
    override suspend fun removeMember(id: String, member: Member): Response<ChatGroup> = chatgroupApiService.removeMember(id, member)
    override suspend fun updateMember(id: String, member: Member): Response<ChatGroup> = chatgroupApiService.updateMember(id, member)
    override suspend fun getMember(id: String): Response<List<Member>> = chatgroupApiService.getMember(id)

}