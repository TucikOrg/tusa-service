package com.coltsclub.tusa.app.dto

import kotlinx.serialization.Serializable

@Serializable
data class ChatResponse(
    val id: Long?,
    val firstUserId: Long,
    val secondUserId: Long,
    val firsUserName: String,
    val secondUserName: String,
    val firstUserUniqueName: String?,
    val secondUserUniqueName: String?,
    val updateTime: Long,
    val deleted: Boolean
)