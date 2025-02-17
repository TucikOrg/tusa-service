package com.coltsclub.tusa.app.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id


@Entity(name = "chat")
class ChatEntity(
    var firstUserId: Long,
    val secondUserId: Long,
    var firsUserName: String,
    var secondUserName: String,
    var firstUserUniqueName: String?,
    var secondUserUniqueName: String?,
) {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    val id: Long? = null
}