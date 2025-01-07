package com.coltsclub.tusa.app.dto

import kotlinx.serialization.Serializable

@Serializable
data class NewChat(
    val chatId: Long,
    val ownerId: Long,
    val toId: Long,
    val lastMessage: String,
    val lastMessageOwner: Long
)