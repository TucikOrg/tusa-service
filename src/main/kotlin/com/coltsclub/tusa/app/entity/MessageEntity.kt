package com.coltsclub.tusa.app.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.time.LocalDateTime

@Entity(name = "message")
class MessageEntity(
    var firstUserId: Long,
    var secondUserId: Long,
    var senderId: Long,
    val message: String,
    val creation: LocalDateTime = LocalDateTime.now(),
) {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    val id: Long? = null
}