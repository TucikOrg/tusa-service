package com.coltsclub.tusa.app.dto.tickets

data class CreateEventDto(
    val name: String,
    val description: String,
    val locationId: Long,
    val startTime: Long,
    val endTime: Long,
    val maxTickets: Int,
    val price: Int,
    val image: String
)