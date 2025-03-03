package com.coltsclub.tusa.app.repository

import com.coltsclub.tusa.app.entity.AvatarEntity
import java.util.Optional
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface AvatarRepository: CrudRepository<AvatarEntity, Long> {
    @Query("SELECT e FROM avatar e WHERE e.ownerId = :ownerId ORDER BY e.creation DESC LIMIT 1")
    fun findLatestByOwnerId(ownerId: Long): Optional<AvatarEntity>

    fun findByOwnerIdAndCreationGreaterThan(
        ownerId: Long,
        creation: Long
    ): List<AvatarEntity>?
}