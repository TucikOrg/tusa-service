package com.coltsclub.tusa.app.dto

import kotlinx.serialization.Serializable

@Serializable
data class ImageDto(
    val ownerId: Long,
    val localFilePathId: String,
    val image: ByteArray?
)