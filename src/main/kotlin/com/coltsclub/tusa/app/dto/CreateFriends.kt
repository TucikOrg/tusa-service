package com.coltsclub.tusa.app.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateFriends(
    val startFrom: Int,
    val count: Int
)