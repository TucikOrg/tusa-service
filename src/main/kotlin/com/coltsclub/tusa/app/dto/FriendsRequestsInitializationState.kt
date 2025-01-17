package com.coltsclub.tusa.app.dto

import kotlinx.serialization.Serializable

@Serializable
data class FriendsRequestsInitializationState(
    val friends: List<FriendRequestDto>,
    val timePoint: Long
)