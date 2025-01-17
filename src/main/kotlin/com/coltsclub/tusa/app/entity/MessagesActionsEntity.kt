package com.coltsclub.tusa.app.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.time.LocalDateTime

@Entity(name = "message_state")
data class MessagesActionsEntity(
    val messageId: Long,
    val firstUserId: Long,
    val secondUserId: Long,
    val senderId: Long,
    val message: String,
    val messageCreation: LocalDateTime = LocalDateTime.now(),
    val actionType: MessagesActionType,
    val actionTime: Long
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
}