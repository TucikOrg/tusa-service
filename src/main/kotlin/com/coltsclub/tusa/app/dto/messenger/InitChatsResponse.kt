package com.coltsclub.tusa.app.dto.messenger

import com.coltsclub.tusa.app.dto.ChatResponse
import kotlinx.serialization.Serializable

@Serializable
data class InitChatsResponse(
    val chats: List<ChatResponse>,
    val timePoint: Long
)