package com.coltsclub.tusa.app.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.time.LocalDateTime

@Entity(name = "avatar")
class AvatarEntity(
    var ownerId: Long,
    val avatar: ByteArray,
    val creation: LocalDateTime = LocalDateTime.now(),
) {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    val id: Long? = null
}