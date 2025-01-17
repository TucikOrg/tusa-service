package com.coltsclub.tusa.app.repository

import com.coltsclub.tusa.app.entity.FriendsRequestsActionsEntity
import org.springframework.data.jpa.repository.JpaRepository

interface FriendsRequestsActionsRepository : JpaRepository<FriendsRequestsActionsEntity, Long> {
    fun findAllByFirstUserIdOrSecondUserIdAndActionTimeGreaterThan(firstUserId: Long, secondUserId: Long, actionTime: Long): List<FriendsRequestsActionsEntity>
}