package com.coltsclub.tusa.app.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.time.LocalDateTime
import java.time.ZoneOffset


@Entity(name = "chat")
class ChatEntity(
    var firstUserId: Long,
    val secondUserId: Long,
    var firsUserName: String,
    var secondUserName: String,
    var firstUserUniqueName: String?,
    var secondUserUniqueName: String?,
    var updateTime: Long = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC),
    var deleted: Boolean = false
) {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    val id: Long? = null
}