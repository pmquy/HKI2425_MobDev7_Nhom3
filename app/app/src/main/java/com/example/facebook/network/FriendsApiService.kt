package com.example.facebook.network

import com.example.facebook.model.FriendAccept
import com.example.facebook.model.FriendRequest
import com.example.facebook.model.GetFriendResponse
import com.example.facebook.model.GetFriendSuggestionsResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface FriendsApiService {
    @POST("api/v1/friend/request")
    suspend fun request(@Body data: FriendRequest): Response<Void>

    @POST("api/v1/friend/accept")
    suspend fun accept(@Body data: FriendAccept): Response<Void>

    @POST("api/v1/friend/decline")
    suspend fun decline(@Body data: FriendAccept): Response<Void>

    @POST("api/v1/friend/revoke")
    suspend fun revoke(@Body data: FriendRequest): Response<Void>

    @POST("api/v1/friend/disfriend")
    suspend fun disfriend(@Body data: FriendAccept): Response<Void>

    @GET("api/v1/friend")
    suspend fun getAll(
        @Query("offset") offset: Int?,
        @Query("limit") limit: Int?,
        @Query("q") q: String?,
    ): Response<GetFriendResponse>

    @GET("api/v1/friend/suggestions")
    suspend fun getSuggestions(
        @Query("offset") offset: Int?,
        @Query("limit") limit: Int?,
        @Query("q") q: String?,
    ): Response<GetFriendSuggestionsResponse>
}