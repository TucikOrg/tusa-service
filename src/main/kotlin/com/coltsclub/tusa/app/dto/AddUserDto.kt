package com.coltsclub.tusa.app.dto

import kotlinx.serialization.Serializable

@Serializable
data class AddUserDto(
    val uniqueName: String,
    val gmail: String
)