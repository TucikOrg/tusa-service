package com.coltsclub.tusa.app.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.time.LocalDateTime
import java.time.ZoneOffset

@Entity(name = "friendRequest")
data class FriendRequestEntity(
    val firstUserId: Long,
    val secondUserId: Long,
    val firstUserName: String,
    val secondUserName: String,
    val firstUserUniqueName: String?,
    val secondUserUniqueName: String?,
    val actorId: Long,
    val date: LocalDateTime = LocalDateTime.now(ZoneOffset.UTC)
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
}