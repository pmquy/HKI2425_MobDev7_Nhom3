package com.example.facebook.network

import com.example.facebook.model.ChatGroup
import com.example.facebook.model.GetAllChatGroupsResponse
import com.example.facebook.model.GetMessagesResponse
import com.example.facebook.model.Member
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query


interface ChatgroupApiService {

    @GET("api/v1/chatgroup/{id}")
    suspend fun getById(@Path("id") id: String): Response<ChatGroup>

    @POST("api/v1/chatgroups")
    suspend fun create(@Body chatGroup: ChatGroup): Response<ChatGroup>

    @PUT("api/v1/chatgroup/{id}")
    suspend fun updateById(@Path("id") id: String, @Body chatGroup: ChatGroup): Response<ChatGroup>

    @DELETE("api/v1/chatgroup/{id}")
    suspend fun deleteById(@Path("id") id: String): Response<Void>

    @GET("api/v1/chatgroup")
    suspend fun getAll(@Query("offset") page: Int, @Query("limit") limit: Int, @Query("q") query: String): Response<GetAllChatGroupsResponse>

    @POST("api/v1/chatgroup/{id}/member")
    suspend fun addMembers(@Path("id") id: String, @Body members: List<Member>): Response<ChatGroup>

    @DELETE("api/v1/chatgroup/{id}/member")
    suspend fun removeMember(@Path("id") id: String, @Body member: Member): Response<ChatGroup>

    @PUT("api/v1/chatgroup/{id}/member")
    suspend fun updateMember(@Path("id") id: String, @Body member: Member): Response<ChatGroup>

    @GET("api/v1/chatgroup/{id}/member")
    suspend fun getMember(@Path("id") id: String): Response<List<Member>>

    @GET("api/v1/chatgroup/{id}/message")
    suspend fun getMessage(@Path("id") id: String, @Query("page") page: Int, @Query("limit") limit: Int, @Query("q") query: String): Response<GetMessagesResponse>
}