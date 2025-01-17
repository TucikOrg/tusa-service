package com.coltsclub.tusa.app.dto

import kotlinx.serialization.Serializable

@Serializable
data class User (
    val id: Long,
    val name: String,
    val uniqueName: String?,
    val gmail: String
)