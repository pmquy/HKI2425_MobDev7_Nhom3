package com.example.facebook.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val _id: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    val password: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val avatar: String = "",
    val createdAt: String = "",
    val updatedAt: String = "",
    val friendStatus: String = "",
)

@Serializable
data class LoginRequest(
    val email: String = "",
    val password: String = "",
)

@Serializable
data class OtpRequest(
    val email: String ="",
    val otp: String= ""
)

@Serializable
data class GetUsersResponse(
    val data: List<User>,
    val hasMore: Boolean,
)