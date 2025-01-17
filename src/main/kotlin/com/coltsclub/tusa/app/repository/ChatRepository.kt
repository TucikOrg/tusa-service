package com.coltsclub.tusa.app.repository

import com.coltsclub.tusa.app.entity.ChatEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ChatRepository: CrudRepository<ChatEntity, Long> {
    fun findByFirstUserIdOrSecondUserId(firstUserId: Long, secondUserId: Long, pageable: Pageable): Page<ChatEntity>

    @Query("SELECT COUNT(c) FROM chat c WHERE (c.firstUserId = :firstUserId AND c.secondUserId = :secondUserId) OR (c.firstUserId = :secondUserId AND c.secondUserId = :firstUserId)")
    fun countChats(firstUserId: Long, secondUserId: Long): Long
}