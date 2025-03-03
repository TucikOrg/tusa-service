package com.coltsclub.tusa.app.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.time.LocalDateTime
import java.time.ZoneOffset
import org.checkerframework.common.aliasing.qual.Unique

@Entity(name = "message")
class MessageEntity(
    var firstUserId: Long,
    var secondUserId: Long,
    var senderId: Long,
    val message: String,
    @Column(unique = true)
    val temporaryId: String,
    val payload: List<String>,
    val updateTime: Long = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC),
    var deleted: Boolean
) {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    val id: Long? = null
}