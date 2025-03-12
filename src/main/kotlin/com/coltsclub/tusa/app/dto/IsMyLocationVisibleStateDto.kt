package com.coltsclub.tusa.app.dto

import kotlinx.serialization.Serializable

@Serializable
data class IsMyLocationVisibleStateDto(
    val isMyLocationVisible: Boolean,
    val ownerId: Long
)