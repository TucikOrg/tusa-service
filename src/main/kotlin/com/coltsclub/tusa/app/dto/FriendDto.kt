package com.coltsclub.tusa.app.dto

import kotlinx.serialization.Serializable

@Serializable
data class FriendDto(
    val id: Long,
    val name: String,
    val uniqueName: String?,
    val lastOnlineTime: Long,
    val updateTime: Long,
    val deleted: Boolean
)