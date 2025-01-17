package com.coltsclub.tusa.app.dto

import com.coltsclub.tusa.app.entity.AvatarActionType
import kotlinx.serialization.Serializable

@Serializable
data class AvatarAction(
    val ownerId: Long,
    val actionType: AvatarActionType,
    val actionTime: Long
)