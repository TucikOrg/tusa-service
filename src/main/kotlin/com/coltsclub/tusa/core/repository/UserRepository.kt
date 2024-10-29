package com.coltsclub.tusa.core.repository

import com.coltsclub.tusa.core.entity.UserEntity
import java.util.Optional
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface UserRepository : JpaRepository<UserEntity, Long> {
    fun findByPhone(phone: String): Optional<UserEntity>
    fun findByUserUniqueName(userUniqueName: String): Optional<UserEntity>

    @Query("SELECT e FROM appUser e WHERE e.userUniqueName LIKE %:name% AND e.id != :id")
    fun findCandidateFriends(name: String, id: Long, pageable: Pageable): List<UserEntity>
}