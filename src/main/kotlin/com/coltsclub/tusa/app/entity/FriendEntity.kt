package com.coltsclub.tusa.app.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.time.LocalDateTime
import java.time.ZoneOffset

@Entity(name = "friend")
data class FriendEntity(
    val firstUserId: Long,
    val secondUserId: Long,
    var firstUserName: String,
    var secondUserName: String,
    var firstUserUniqueName: String?,
    var secondUserUniqueName: String?,
    var firstUserLastOnlineTime: LocalDateTime,
    var secondUserLastOnlineTime: LocalDateTime,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
}