package com.coltsclub.tusa.app.dto.messenger

import com.coltsclub.tusa.app.dto.MessageResponse
import com.coltsclub.tusa.app.entity.MessagesActionType
import kotlinx.serialization.Serializable

@Serializable
data class MessagesAction(
    val message: MessageResponse,
    val actionType: MessagesActionType,
    val actionTime: Long,
)