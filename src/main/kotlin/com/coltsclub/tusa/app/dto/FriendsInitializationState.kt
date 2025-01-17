package com.coltsclub.tusa.app.dto

import kotlinx.serialization.Serializable

@Serializable
data class FriendsInitializationState(
    val friends: List<FriendDto>,
    val timePoint: Long
)