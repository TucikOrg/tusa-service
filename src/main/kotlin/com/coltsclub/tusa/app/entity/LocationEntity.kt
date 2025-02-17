package com.coltsclub.tusa.app.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.time.LocalDateTime
import java.time.ZoneOffset

@Entity(name = "location")
class LocationEntity(
    var ownerId: Long,
    val latitude: String,
    val longitude: String,
    val creation: LocalDateTime = LocalDateTime.now(ZoneOffset.UTC)
) {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    val id: Long? = null
}