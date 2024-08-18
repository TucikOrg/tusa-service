package com.coltsclub.tusa.core.model

data class AuthenticateInstruction(
    val jwt: String,
    val needTransferLocationToken: Boolean
)