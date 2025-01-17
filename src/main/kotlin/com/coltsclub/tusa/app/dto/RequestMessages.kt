package com.coltsclub.tusa.app.dto

import kotlinx.serialization.Serializable

@Serializable
data class RequestMessages(
    val withUserId: Long,
    val page: Int,
    val size: Int,
)