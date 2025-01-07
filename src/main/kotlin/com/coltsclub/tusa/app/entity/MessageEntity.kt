package com.coltsclub.tusa.app.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.time.LocalDateTime

@Entity(name = "message")
class MessageEntity(
    var ownerId: Long,
    var toId: Long,
    var chatId: Long,
    var payload: List<Long>,
    val message: String,
    val creation: LocalDateTime = LocalDateTime.now(),
    val deletedOwner: Boolean,
    val deletedTo: Boolean,
    val changed: Boolean,
    val read: Boolean
) {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    val id: Long? = null
}