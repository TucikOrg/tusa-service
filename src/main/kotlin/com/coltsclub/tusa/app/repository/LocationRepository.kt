package com.coltsclub.tusa.app.repository

import com.coltsclub.tusa.app.entity.LocationEntity
import java.util.Optional
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface LocationRepository: CrudRepository<LocationEntity, Long> {
    @Query("SELECT e FROM location e WHERE e.ownerId = :ownerId ORDER BY e.creation DESC LIMIT 1")
    fun findTopByOwnerIdAndByOrderByCreationDesc(ownerId: Long): Optional<LocationEntity>

    @Query("SELECT e FROM location e WHERE e.ownerId = :ownerId")
    fun findAllByOwnerId(ownerId: Long): List<LocationEntity>
}