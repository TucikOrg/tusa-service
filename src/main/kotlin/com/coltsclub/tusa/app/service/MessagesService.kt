package com.coltsclub.tusa.app.service

import com.coltsclub.tusa.app.dto.MessageResponse
import com.coltsclub.tusa.app.dto.messenger.MessagesAction
import com.coltsclub.tusa.app.entity.MessageEntity
import com.coltsclub.tusa.app.repository.MessageRepository
import com.coltsclub.tusa.app.repository.MessagesActionsRepository
import com.coltsclub.tusa.core.AlineTwoLongsIds
import java.time.ZoneOffset
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class MessagesService(
    private val messageRepository: MessageRepository,
    private val messagesActionsRepository: MessagesActionsRepository
) {
    fun getInitMessages(userId: Long, size: Int): List<MessageResponse> {
        return messageRepository.findTopPerUserGroup(userId, size).map {
            MessageResponse(
                id = it.id!!,
                firstUserId = it.firstUserId,
                secondUserId = it.secondUserId,
                message = it.message,
                creation = it.creation.toEpochSecond(ZoneOffset.UTC),
                senderId = it.senderId,
                temporaryId = it.temporaryId
            )
        }
    }

    fun getMessages(userId: Long, withUserId: Long, pageable: Pageable) : Page<MessageEntity> {
        val ids = AlineTwoLongsIds.aline(userId, withUserId)
        val first = ids.first
        val second = ids.second
        return messageRepository.findByFirstUserIdAndSecondUserId(first, second, pageable)
    }

    fun getActions(userId: Long, actionTime: Long): List<MessagesAction> {
        val actions = messagesActionsRepository.findAllByFirstUserIdOrSecondUserIdAndActionTimeGreaterThan(userId, userId, actionTime)
        return actions.map {
            MessagesAction(
                message = MessageResponse(
                    id = it.messageId,
                    firstUserId = it.firstUserId,
                    secondUserId = it.secondUserId,
                    message = it.message,
                    creation = it.messageCreation.toEpochSecond(ZoneOffset.UTC),
                    senderId = it.senderId,
                    temporaryId = it.messageTemporaryId
                ),
                actionType = it.actionType,
                actionTime = it.actionTime
            )
        }
    }
}