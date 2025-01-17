package com.coltsclub.tusa.app.dto

import com.coltsclub.tusa.app.entity.FriendsActionType
import kotlinx.serialization.Serializable

@Serializable
data class FriendRequestActionDto(
    val friendRequestDto: FriendRequestDto,
    val friendsActionType: FriendsActionType,
    val actionTime: Long
)