package com.coltsclub.tusa.app.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.time.LocalDateTime
import java.time.ZoneOffset

@Entity(name = "image")
class ImageEntity(
    var ownerId: Long,
    val image: ByteArray,
    val creation: LocalDateTime = LocalDateTime.now(ZoneOffset.UTC),
    val chatId: Long? = null,
    val localFilePathId: String
) {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    val id: Long? = null
}