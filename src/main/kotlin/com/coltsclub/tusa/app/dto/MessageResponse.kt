package com.coltsclub.tusa.app.dto

import kotlinx.serialization.Serializable

@Serializable
data class MessageResponse(
    var id: Long? = null,
    var firstUserId: Long,
    var secondUserId: Long,
    var senderId: Long,
    val message: String,
    val creation: Long,
)