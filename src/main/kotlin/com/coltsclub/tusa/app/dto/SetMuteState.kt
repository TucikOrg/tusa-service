package com.coltsclub.tusa.app.dto

import kotlinx.serialization.Serializable


@Serializable
data class SetMuteState(
    val toId: Long,
    val state: Boolean
)