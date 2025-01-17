package com.coltsclub.tusa.app.repository

import com.coltsclub.tusa.app.entity.FriendsActionsEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface FriendsActionsRepository : JpaRepository<FriendsActionsEntity, Long> {
    @Query(
        "SELECT fa FROM friend_state fa WHERE (fa.firstUserId = :firstUserId OR fa.secondUserId = :secondUserId) AND fa.actionTime > :actionTime",
    )
    fun findAllByFirstUserIdOrSecondUserIdAndActionTimeGreaterThan(
        firstUserId: Long,
        secondUserId: Long,
        actionTime: Long
    ): List<FriendsActionsEntity>
}