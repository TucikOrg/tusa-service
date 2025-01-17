package com.coltsclub.tusa.app.repository

import com.coltsclub.tusa.app.entity.FriendRequestEntity
import java.util.Optional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface FriendRequestRepository : JpaRepository<FriendRequestEntity, Long> {
    fun findByFirstUserIdAndSecondUserId(firstUserId: Long, secondUserId: Long): FriendRequestEntity?
    fun findByFirstUserIdOrSecondUserId(firstUserId: Long, secondUserId: Long): List<FriendRequestEntity>

    fun deleteByFirstUserIdAndSecondUserId(firstUserId: Long, secondUserId: Long): Int
}