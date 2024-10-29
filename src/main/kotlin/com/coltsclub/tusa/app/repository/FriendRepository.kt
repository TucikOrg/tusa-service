package com.coltsclub.tusa.app.repository

import com.coltsclub.tusa.app.entity.FriendEntity
import java.util.Optional
import org.springframework.data.jpa.repository.JpaRepository

interface FriendRepository : JpaRepository<FriendEntity, Long> {
    fun findByFromUserId(fromUserId: Long): List<FriendEntity>
    fun findByFromUserIdAndToUserId(fromUserId: Long, toUserId: Long): Optional<FriendEntity>
    fun deleteByFromUserIdAndToUserId(fromUserId: Long, toUserId: Long)
    fun findAllByFromUserIdAndToUserIdIn(fromUserId: Long, toUserId: List<Long>): List<FriendEntity>
}