package com.coltsclub.tusa.app.repository

import com.coltsclub.tusa.app.entity.ChatsActionsEntity
import com.coltsclub.tusa.app.entity.MessagesActionsEntity
import org.springframework.data.repository.CrudRepository

interface ChatsActionsRepository : CrudRepository<ChatsActionsEntity, Long> {
    fun findAllByFirstUserIdOrSecondUserIdAndActionTimeGreaterThan(firstUserId: Long, secondUserId: Long, actionTime: Long): List<ChatsActionsEntity>
}