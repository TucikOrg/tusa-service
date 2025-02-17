package com.coltsclub.tusa.app.dto.messenger

import kotlinx.serialization.Serializable

@Serializable
data class WritingMessage(
    val toUserId: Long,
    val message: String,
    var fromUserId: Long
)