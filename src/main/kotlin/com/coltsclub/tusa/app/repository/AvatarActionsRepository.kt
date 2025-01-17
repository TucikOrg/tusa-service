package com.coltsclub.tusa.app.repository

import com.coltsclub.tusa.app.entity.AvatarActionsEntity
import com.coltsclub.tusa.app.entity.ChatsActionsEntity
import org.springframework.data.repository.CrudRepository

interface AvatarActionsRepository : CrudRepository<AvatarActionsEntity, Long> {
    fun findAllByOwnerIdInAndActionTimeGreaterThan(ownerId: List<Long>, actionTime: Long): List<AvatarActionsEntity>
}