package com.coltsclub.tusa.core.dto

import com.coltsclub.tusa.core.model.AuthenticateInstruction

data class LoginResponseDto(
    val token: AuthenticateInstruction
)