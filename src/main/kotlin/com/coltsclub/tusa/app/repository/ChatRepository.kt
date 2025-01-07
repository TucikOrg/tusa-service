package com.coltsclub.tusa.app.repository

import com.coltsclub.tusa.app.entity.ChatEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ChatRepository: CrudRepository<ChatEntity, Long> {
    fun findAllByOwnerId(ownerId: Long, pageable: Pageable): Page<ChatEntity>

    fun findByToIdAndOwnerId(toId: Long, ownerId: Long): ChatEntity?
}