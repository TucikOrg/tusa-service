package com.coltsclub.tusa.app.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id


@Entity(name = "chat")
class ChatEntity(
    var ownerId: Long,
    val toId: Long,
    var muted: Boolean,
    val chatId: Long,
    var lastMessage: String,
    var lastMessageOwner: Long,
    var deleted: Boolean = false
) {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    val id: Long? = null
}