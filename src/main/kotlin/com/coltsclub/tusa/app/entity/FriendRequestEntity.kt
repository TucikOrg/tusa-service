package com.coltsclub.tusa.app.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.time.LocalDateTime

@Entity(name = "friendRequest")
data class FriendRequestEntity(
    val fromPhone: String,
    val toPhone: String,
    val date: LocalDateTime = LocalDateTime.now(),
    val activated: Boolean = false
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
}