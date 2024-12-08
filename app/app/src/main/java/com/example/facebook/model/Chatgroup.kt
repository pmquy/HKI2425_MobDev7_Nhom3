package com.example.facebook.model

import kotlinx.serialization.Serializable

@Serializable
data class Member(
    val user: String = "",
    val role: String = "",
    val createdAt: String = "",
    val updatedAt: String = "",
)


@Serializable
data class LastMessage(
    val name: String = "",
    val content: String = "",
    val createdAt: String = "",
)

@Serializable
data class ChatGroup(
    val _id: String = "",
    val name: String = "",
    val avatar: String = "",
    val users: List<Member> = listOf(),
    val createdAt: String = "",
    val updatedAt: String = "",
    val lastMessage: LastMessage? = null,
)

@Serializable
data class GetAllChatGroupsResponse(
    val data: List<ChatGroup>,
    val hasMore: Boolean,
)

@Serializable
data class  GetMessagesResponse(
    val data: List<Message>,
    val hasMore: Boolean,
)