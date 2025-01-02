package com.coltsclub.tusa.app.dto

import kotlinx.serialization.Serializable

@Serializable
data class FakeLocation(
    val latitude: Float,
    val longitude: Float,
    val userId: Long
)