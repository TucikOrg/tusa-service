package com.coltsclub.tusa.app.dto

import kotlinx.serialization.Serializable

@Serializable
data class LocationDto(
    val ownerId: Long,
    val latitude: Float,
    val longitude: Float
)