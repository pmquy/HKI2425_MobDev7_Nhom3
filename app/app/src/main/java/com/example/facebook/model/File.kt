package com.example.facebook.model

import kotlinx.serialization.Serializable

@Serializable
data class File(
    val _id: String = "",
    val url: String = "",
    val blurUrl: String = "",
    val type: String = "",
    val status: String = "",
    val name: String = "",
    val description: String = "",
    val createdAt: String = "",
    val updatedAt: String = "",
)

@Serializable
data class GetSystemFileResponse(
    val hasMore: Boolean,
    val data: List<File>,
)
