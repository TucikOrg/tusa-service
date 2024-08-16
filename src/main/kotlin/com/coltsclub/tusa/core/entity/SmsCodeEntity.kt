package com.coltsclub.tusa.core.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.time.LocalDateTime

@Entity(name = "smsCode")
data class SmsCodeEntity(
    val phone: String,
    val code: String,
    val expiredAt: LocalDateTime,
    var activated: Boolean = false
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
}