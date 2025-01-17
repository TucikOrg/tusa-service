package com.coltsclub.tusa.app.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity(name = "friend_state")
data class FriendsActionsEntity(
    val actionTime: Long,
    val firstUserId: Long,
    val secondUserId: Long,
    val firstUserName: String,
    val secondUserName: String,
    val firstUserUniqueName: String?,
    val secondUserUniqueName: String?,
    val actionType: FriendsActionType,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
}