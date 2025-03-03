package com.coltsclub.tusa.app.service

import com.coltsclub.tusa.app.dto.MessageResponse
import com.coltsclub.tusa.app.entity.MessageEntity
import com.coltsclub.tusa.app.repository.MessageRepository
import com.coltsclub.tusa.core.AlineTwoLongsIds
import java.time.ZoneOffset
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class MessagesService(
    private val messageRepository: MessageRepository,
) {
    fun getInitMessages(userId: Long, size: Int): List<MessageResponse> {
        return messageRepository.findTopPerUserGroup(userId, size).map {
            MessageResponse(
                id = it.id!!,
                firstUserId = it.firstUserId,
                secondUserId = it.secondUserId,
                message = it.message,
                updateTime = it.updateTime,
                senderId = it.senderId,
                temporaryId = it.temporaryId,
                payload = it.payload.joinToString(separator = ","),
                deleted = it.deleted
            )
        }
    }

    fun getMessages(userId: Long, withUserId: Long, pageable: Pageable) : Page<MessageEntity> {
        val ids = AlineTwoLongsIds.aline(userId, withUserId)
        val first = ids.first
        val second = ids.second
        return messageRepository.findByFirstUserIdAndSecondUserIdAndDeleted(first, second, pageable, deleted = false)
    }

    fun getActions(userId: Long, actionTime: Long): List<MessageResponse> {
        val messages = messageRepository.findAllByFirstUserIdOrSecondUserIdAndUpdateTimeGreaterThan(userId, userId, actionTime)
        return messages.map {
            MessageResponse(
                id = it.id,
                firstUserId = it.firstUserId,
                secondUserId = it.secondUserId,
                message = it.message,
                updateTime = it.updateTime,
                senderId = it.senderId,
                temporaryId = it.temporaryId,
                payload = it.payload.joinToString(separator = ","),
                deleted = it.deleted
            )
        }
    }
}