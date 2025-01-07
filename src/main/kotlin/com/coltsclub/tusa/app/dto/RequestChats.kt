package com.coltsclub.tusa.app.dto

import kotlinx.serialization.Serializable

@Serializable
data class RequestChats(
    val page: Int,
    val size: Int
)