package com.coltsclub.tusa.app.dto

import com.coltsclub.tusa.app.entity.FriendsActionType
import kotlinx.serialization.Serializable

@Serializable
data class FriendActionDto(
    val friendDto: FriendDto,
    val friendsActionType: FriendsActionType,
    val actionTime: Long
)