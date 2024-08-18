package com.coltsclub.tusa.core.dto

data class LoginDto(
    val phone: String,
    val code: String,
    val device: String
)