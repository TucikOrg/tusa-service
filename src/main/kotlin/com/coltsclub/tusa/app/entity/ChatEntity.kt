package com.coltsclub.tusa.app.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id


@Entity(name = "chat")
class ChatEntity(
    var firstUserId: Long,
    val secondUserId: Long,
    val firsUserName: String,
    val secondUserName: String,
    val firstUserUniqueName: String?,
    val secondUserUniqueName: String?,
) {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    val id: Long? = null
}