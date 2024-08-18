package com.coltsclub.tusa.app.repository

import com.coltsclub.tusa.app.entity.FriendEntity
import java.util.Optional
import org.springframework.data.jpa.repository.JpaRepository

interface FriendRepository : JpaRepository<FriendEntity, Long> {
    fun findByOwner(owner: String): List<FriendEntity>
    fun findByOwnerAndHasFriend(owner: String, hasFriend: String): Optional<FriendEntity>
    fun deleteByOwnerAndHasFriend(owner: String, hasFriend: String)
}