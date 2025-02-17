package com.coltsclub.tusa.app.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.time.LocalDateTime
import java.time.ZoneOffset

@Entity(name = "message_state")
data class MessagesActionsEntity(
    val messageId: Long,
    val firstUserId: Long,
    val secondUserId: Long,
    val senderId: Long,
    val message: String,
    val messageTemporaryId: String,
    val messageCreation: LocalDateTime = LocalDateTime.now(ZoneOffset.UTC),
    val actionType: MessagesActionType,
    val actionTime: Long = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC)
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
}