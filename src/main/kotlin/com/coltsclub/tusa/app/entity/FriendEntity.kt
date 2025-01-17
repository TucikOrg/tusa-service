package com.coltsclub.tusa.app.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity(name = "friend")
data class FriendEntity(
    val firstUserId: Long,
    val secondUserId: Long,
    val firstUserName: String,
    val secondUserName: String,
    val firstUserUniqueName: String?,
    val secondUserUniqueName: String?,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
}