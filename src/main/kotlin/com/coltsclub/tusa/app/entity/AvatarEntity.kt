package com.coltsclub.tusa.app.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.time.LocalDateTime

@Entity(name = "avatar")
class AvatarEntity(
    val owner: String,
    val creation: LocalDateTime = LocalDateTime.now(),
    val avatar: ByteArray
) {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    val id: Long? = null
}