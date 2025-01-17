package com.coltsclub.tusa.app.dto.messenger

import com.coltsclub.tusa.app.dto.MessageResponse
import kotlinx.serialization.Serializable

@Serializable
data class InitMessagesResponse(
    val messages: List<MessageResponse>,
    val timePoint: Long
)