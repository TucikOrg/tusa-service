package com.coltsclub.tusa.core.dto

data class LoginResponseDto(
    val uniqueName: String,
    val name: String,
    val jwt: String,
    val needTransferLocationToken: Boolean
)