package com.coltsclub.tusa.app.dto

import kotlinx.serialization.Serializable

@Serializable
data class RequestPage(
    val page: Int,
    val pageSize: Int
) {
}