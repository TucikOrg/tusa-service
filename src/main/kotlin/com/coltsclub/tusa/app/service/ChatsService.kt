package com.coltsclub.tusa.app.service

import com.coltsclub.tusa.app.dto.ChatResponse
import com.coltsclub.tusa.app.dto.SendMessageResult
import com.coltsclub.tusa.app.entity.ChatEntity
import com.coltsclub.tusa.app.entity.MessageEntity
import com.coltsclub.tusa.app.exceptions.ChatUserDeletedException
import com.coltsclub.tusa.app.repository.ChatRepository
import com.coltsclub.tusa.app.repository.MessageRepository
import com.coltsclub.tusa.core.AlineTwoLongsIds
import com.coltsclub.tusa.core.repository.UserRepository
import jakarta.transaction.Transactional
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.jvm.optionals.getOrNull
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class ChatsService(
    private val chatsRepository: ChatRepository,
    private val messageRepository: MessageRepository,
    private val userRepository: UserRepository
) {
    @Transactional
    fun sendMessage(
        userId: Long,
        toUserId: Long,
        message: String,
        tempId: String,
        payload: List<String>
    ): SendMessageResult {
        var chatCreated = false
        val ids = AlineTwoLongsIds.aline(userId, toUserId)
        val firstId = ids.first
        val secondId = ids.second

        // сохраняем сообщение
        val savedMessage = messageRepository.save(
            MessageEntity(
                firstUserId = firstId,
                secondUserId = secondId,
                senderId = userId,
                message = message,
                temporaryId = tempId,
                payload = payload,
                deleted = false
            )
        )

        // проверяем наличие чата
        val chatExist = chatsRepository.countChats(userId, toUserId) > 0
        if (!chatExist) {
            val firstUser = userRepository.findById(firstId).getOrNull()?: throw ChatUserDeletedException()
            val secondUser = userRepository.findById(secondId).getOrNull()?: throw ChatUserDeletedException()

            // создаем чат
            chatsRepository.save(
                ChatEntity(
                    firstUserId = firstId,
                    secondUserId = secondId,
                    firsUserName = firstUser.name,
                    secondUserName = secondUser.name,
                    firstUserUniqueName = firstUser.userUniqueName,
                    secondUserUniqueName = secondUser.userUniqueName,
                    deleted = false
                )
            )
            chatCreated = true
        }

        return SendMessageResult(chatCreated)
    }

    fun getChats(userId: Long, pageable: Pageable): Page<ChatEntity> {
        return chatsRepository.findByFirstUserIdOrSecondUserIdAndDeleted(userId, userId, pageable, deleted = false)
    }

    fun getActions(userId: Long, actionTime: Long): List<ChatResponse> {
        val chats = chatsRepository.findAllByFirstUserIdOrSecondUserIdAndUpdateTimeGreaterThan(
            userId, userId, actionTime
        )
        return chats.map {
            ChatResponse(
                id = it.id,
                firstUserId = it.firstUserId,
                secondUserId = it.secondUserId,
                firsUserName = it.firsUserName,
                secondUserName = it.secondUserName,
                firstUserUniqueName = it.firstUserUniqueName,
                secondUserUniqueName = it.secondUserUniqueName,
                updateTime = it.updateTime,
                deleted = it.deleted
            )
        }
    }

    fun getInitChats(id: Long, size: Int): List<ChatResponse> {
        val chats = chatsRepository.findByFirstUserIdOrSecondUserIdAndDeleted(id, id, Pageable.unpaged(), deleted = false)
        return chats.map {
            ChatResponse(
                id = it.id!!,
                firstUserId = it.firstUserId,
                secondUserId = it.secondUserId,
                firsUserName = it.firsUserName,
                secondUserName = it.secondUserName,
                firstUserUniqueName = it.firstUserUniqueName,
                secondUserUniqueName = it.secondUserUniqueName,
                updateTime = it.updateTime,
                deleted = it.deleted
            )
        }.toList()
    }

    fun deleteChat(firstId: Long, secondId: Long) {
       chatsRepository.findByFirstUserIdAndSecondUserId(firstId, secondId)?.let {
           it.deleted = true
           it.updateTime = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC)
           chatsRepository.save(it)
       }
    }

    @Transactional
    fun fullClearChatAndMessages(id: Long, toId: Long) {
        val ids = AlineTwoLongsIds.aline(id, toId)
        val firstId = ids.first
        val secondId = ids.second

        // зачищаем чат если есть
        val chat = chatsRepository.findByFirstUserIdAndSecondUserId(firstId, secondId)
        if (chat != null) {
            // сохраняем действие об удалении чата
            // когда клиент получит это сообщение он так же очистит все сообщения чата
            deleteChat(firstId, secondId)
        }

        // зачищаем все сообщения между пользователями
        messageRepository.deleteByFirstUserIdAndSecondUserId(firstId, secondId)
    }
}