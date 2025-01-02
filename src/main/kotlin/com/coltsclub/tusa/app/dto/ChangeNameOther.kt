package com.coltsclub.tusa.app.dto

import kotlinx.serialization.Serializable


@Serializable
data class ChangeNameOther(
    val userId: Long,
    val name: String
)