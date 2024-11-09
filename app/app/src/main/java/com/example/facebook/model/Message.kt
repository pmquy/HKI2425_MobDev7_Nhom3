package com.example.facebook.model

import kotlinx.serialization.Serializable

@Serializable
data class Message(
    var _id: String = "",
    var message: String = "",
    var user: String = "",
    var chatgroup: String = "",
    var files: List<String> = listOf(),
    var createdAt: String = "",
    var updatedAt: String = "",
)
