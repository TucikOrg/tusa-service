package com.coltsclub.tusa.app.dto

import kotlinx.serialization.Serializable

@Serializable
data class MyPage<Item>(
    val items: List<Item>,
    val page: Int,
    val totalPages: Int
)