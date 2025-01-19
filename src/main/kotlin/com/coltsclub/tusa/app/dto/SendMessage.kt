package com.coltsclub.tusa.app.dto

import kotlinx.serialization.Serializable

@Serializable
data class SendMessage(
    val toId: Long,
    val message: String,
    val temporaryId: String,
    val payload: List<Long>
)