package com.coltsclub.tusa.core.repository

import com.coltsclub.tusa.core.entity.TokenEntity
import java.util.Optional
import org.springframework.data.jpa.repository.JpaRepository


interface TokenRepository : JpaRepository<TokenEntity, Long> {
    fun findByToken(token: String): Optional<TokenEntity>
}