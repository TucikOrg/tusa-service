package com.coltsclub.tusa.app.dto

import kotlinx.serialization.Serializable

@Serializable
data class FriendRequestDto(
    val userId: Long,
    val userName: String,
    val userUniqueName: String?,
    val isRequestSender: Boolean
)