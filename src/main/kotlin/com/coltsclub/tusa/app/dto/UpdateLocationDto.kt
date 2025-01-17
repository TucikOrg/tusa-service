package com.coltsclub.tusa.app.dto

import kotlinx.serialization.Serializable

@Serializable
data class UpdateLocationDto(
    val whoId: Long,
    val latitude: Float,
    val longitude: Float
)