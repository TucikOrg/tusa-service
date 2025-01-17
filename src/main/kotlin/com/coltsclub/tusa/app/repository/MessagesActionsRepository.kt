package com.coltsclub.tusa.app.repository

import com.coltsclub.tusa.app.entity.FriendsActionsEntity
import com.coltsclub.tusa.app.entity.MessagesActionsEntity
import org.springframework.data.repository.CrudRepository

interface MessagesActionsRepository : CrudRepository<MessagesActionsEntity, Long> {
    fun findAllByFirstUserIdOrSecondUserIdAndActionTimeGreaterThan(firstUserId: Long, secondUserId: Long, actionTime: Long): List<MessagesActionsEntity>
}