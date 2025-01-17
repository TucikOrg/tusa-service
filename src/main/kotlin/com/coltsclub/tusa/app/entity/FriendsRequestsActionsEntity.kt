package com.coltsclub.tusa.app.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity(name = "friend_request_state")
data class FriendsRequestsActionsEntity(
    val firstUserId: Long,
    val secondUserId: Long,
    val actorId: Long,
    val firstUserName: String,
    val secondUserName: String,
    val firstUserUniqueName: String?,
    val secondUserUniqueName: String?,
    val actionType: FriendsActionType,
    val actionTime: Long
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
}