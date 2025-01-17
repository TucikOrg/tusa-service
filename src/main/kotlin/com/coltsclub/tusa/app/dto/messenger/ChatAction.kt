package com.coltsclub.tusa.app.dto.messenger

import com.coltsclub.tusa.app.dto.ChatResponse
import com.coltsclub.tusa.app.entity.ChatsActionType
import kotlinx.serialization.Serializable

@Serializable
data class ChatAction(
    val chat: ChatResponse,
    val actionType: ChatsActionType,
    val actionTime: Long
)