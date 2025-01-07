package com.coltsclub.tusa.app.service

import com.coltsclub.tusa.app.entity.MessageEntity
import com.coltsclub.tusa.app.repository.MessageRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class MessagesService(
    private val messageRepository: MessageRepository
) {
    fun getMessages(chatId: Long, pageable: Pageable) : Page<MessageEntity> {
        return messageRepository.findByChatId(chatId, pageable)
    }
}