package com.coltsclub.tusa.app.dto

import kotlinx.serialization.Serializable

@Serializable
class AvatarDTO(
    val ownerId: Long,
    val avatar: ByteArray
)