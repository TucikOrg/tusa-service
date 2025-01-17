package com.coltsclub.tusa.app.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import kotlinx.serialization.Serializable

@Serializable
@Entity(name = "chats_state")
data class ChatsActionsEntity(
    val chatId: Long,
    val firstUserId: Long,
    val secondUserId: Long,
    val firsUserName: String,
    val secondUserName: String,
    val firstUserUniqueName: String?,
    val secondUserUniqueName: String?,
    val actionType: ChatsActionType,
    val actionTime: Long
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
}