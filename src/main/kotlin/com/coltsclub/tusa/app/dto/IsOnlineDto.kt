package com.coltsclub.tusa.app.dto

import kotlinx.serialization.Serializable

@Serializable
data class IsOnlineDto(
    val userId: Long,
    val isOnline: Boolean
)