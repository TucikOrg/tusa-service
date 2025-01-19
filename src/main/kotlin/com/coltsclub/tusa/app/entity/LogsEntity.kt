package com.coltsclub.tusa.app.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.time.LocalDateTime

@Entity(name = "logs")
class LogsEntity(
    val message: String,
    @Column(length = 10000)
    val stackTrace: String,
    val thread: String,
    val userId: Long,
    val creation: LocalDateTime = LocalDateTime.now()
) {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    val id: Long? = null
}