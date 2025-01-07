package com.coltsclub.tusa.app.dto

import kotlinx.serialization.Serializable

@Serializable
data class RequestLastMessages(
    val chatId: Long,
    val pageSize: Int,
)