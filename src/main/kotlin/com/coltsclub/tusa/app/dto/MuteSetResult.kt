package com.coltsclub.tusa.app.dto

import kotlinx.serialization.Serializable


@Serializable
data class MuteSetResult(
    val chatId: Long,
    val state: Boolean
)