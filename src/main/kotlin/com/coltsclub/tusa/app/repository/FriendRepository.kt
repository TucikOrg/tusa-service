package com.coltsclub.tusa.app.repository

import com.coltsclub.tusa.app.entity.FriendEntity
import java.util.Optional
import org.springframework.data.jpa.repository.JpaRepository

interface FriendRepository : JpaRepository<FriendEntity, Long> {
    fun findByFirstUserIdOrSecondUserId(firstUserId: Long, secondUserId: Long): List<FriendEntity>

    fun findByFirstUserIdAndSecondUserId(firstUserId: Long, secondUserId: Long): FriendEntity?

    fun findAllByFirstUserIdOrSecondUserIdAndUpdateTimeGreaterThan(
        firstUserId: Long,
        secondUserId: Long,
        updateTime: Long
    ): List<FriendEntity>
}