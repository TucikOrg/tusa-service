package com.coltsclub.tusa.core.repository

import com.coltsclub.tusa.core.entity.UserEntity
import java.util.Optional
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<UserEntity, Long> {
    fun findByPhone(phone: String): Optional<UserEntity>
    fun findByUserUniqueName(userUniqueName: String): Optional<UserEntity>
}