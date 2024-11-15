package com.example.facebook.model

import kotlinx.serialization.Serializable

@Serializable
data class Friend(
    var _id: String = "",
    var from: String = "",
    var to: String = "",
    var status: String = "",
    var createdAt: String = "",
    var updatedAt: String = "",
)

@Serializable
data class FriendRequest(
    val to: String = ""
)

@Serializable
data class FriendAccept(
    val from: String = ""
)

@Serializable
data class GetFriendResponse(
    val hasMore: Boolean,
    val data: List<Friend>
)

@Serializable
data class GetFriendSuggestionsResponse(
    val hasMore: Boolean,
    val data: List<String>
)