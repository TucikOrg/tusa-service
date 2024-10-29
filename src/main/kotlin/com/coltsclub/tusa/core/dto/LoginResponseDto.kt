package com.coltsclub.tusa.core.dto

data class LoginResponseDto(
    val id: Long,
    val uniqueName: String,
    val name: String,
    val jwt: String,
    val phone: String
)