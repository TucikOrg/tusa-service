package com.coltsclub.tusa.app.dto

import kotlinx.serialization.Serializable

@Serializable
data class UsersPage(
    val users: List<User>,
    val pagesCount: Int
)