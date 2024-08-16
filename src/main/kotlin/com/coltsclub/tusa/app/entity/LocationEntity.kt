package com.coltsclub.tusa.app.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.time.LocalDateTime

@Entity(name = "location")
class LocationEntity(
    val identifier: String,
    val latitude: Float,
    val longitude: Float,
    val creation: LocalDateTime = LocalDateTime.now()
) {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    val id: Long? = null
}