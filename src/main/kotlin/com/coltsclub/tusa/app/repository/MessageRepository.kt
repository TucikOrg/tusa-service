package com.coltsclub.tusa.app.repository

import com.coltsclub.tusa.app.entity.MessageEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface MessageRepository: CrudRepository<MessageEntity, Long> {
    fun findByChatId(chatId: Long, pageable: Pageable): Page<MessageEntity>

    @Query("SELECT MAX(e.chatId) FROM message e")
    fun findMaxChatId(): Long?

    fun countByChatId(chatId: Long): Long
}