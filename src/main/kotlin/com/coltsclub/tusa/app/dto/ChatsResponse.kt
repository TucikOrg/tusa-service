package com.coltsclub.tusa.app.dto

import kotlinx.serialization.Serializable

@Serializable
data class ChatsResponse(
    val chats: List<ChatResponse>,
    val totalPages: Int,
    val page: Int
)