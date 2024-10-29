package com.coltsclub.tusa.app.dto

import kotlinx.serialization.Serializable

@Serializable
data class FriendRequestDto(
    val id: Long?,
    val name: String,
    val uniqueName: String?
)