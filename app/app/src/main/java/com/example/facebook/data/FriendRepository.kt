package com.example.facebook.data

import com.example.facebook.model.FriendAccept
import com.example.facebook.model.FriendRequest
import com.example.facebook.model.GetFriendResponse
import com.example.facebook.model.GetFriendSuggestionsResponse
import com.example.facebook.network.FriendsApiService
import retrofit2.Response

interface FriendRepository {
    suspend fun request(to: String)
    suspend fun accept(from: String)
    suspend fun decline(from: String)
    suspend fun revoke(to: String)
    suspend fun disfriend(from: String)
    suspend fun getAll(offset: Int?, limit: Int?, q: String?): Response<GetFriendResponse>
    suspend fun getSuggestions(
        offset: Int?,
        limit: Int?,
        q: String?
    ): Response<GetFriendSuggestionsResponse>
}

class NetworkFriendRepository(
    private val friendApiService: FriendsApiService
) : FriendRepository {
    override suspend fun request(to: String) {
        friendApiService.request(FriendRequest(to))
    }

    override suspend fun accept(from: String) {
        friendApiService.accept(FriendAccept(from))
    }

    override suspend fun decline(from: String) {
        friendApiService.decline(FriendAccept(from))
    }

    override suspend fun revoke(to: String) {
        friendApiService.revoke(FriendRequest(to))
    }

    override suspend fun disfriend(from: String) {
        friendApiService.disfriend(FriendAccept(from))
    }

    override suspend fun getAll(offset: Int?, limit: Int?, q: String?) =
        friendApiService.getAll(offset, limit, q)

    override suspend fun getSuggestions(offset: Int?, limit: Int?, q: String?) =
        friendApiService.getSuggestions(offset, limit, q)
}