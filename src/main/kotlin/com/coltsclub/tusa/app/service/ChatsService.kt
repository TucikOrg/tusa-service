package com.coltsclub.tusa.app.service

import com.coltsclub.tusa.app.dto.ChatResponse
import com.coltsclub.tusa.app.dto.SendMessageResult
import com.coltsclub.tusa.app.dto.messenger.ChatAction
import com.coltsclub.tusa.app.entity.ChatEntity
import com.coltsclub.tusa.app.entity.ChatsActionType
import com.coltsclub.tusa.app.entity.ChatsActionsEntity
import com.coltsclub.tusa.app.entity.MessageEntity
import com.coltsclub.tusa.app.entity.MessagesActionType
import com.coltsclub.tusa.app.entity.MessagesActionsEntity
import com.coltsclub.tusa.app.exceptions.ChatUserDeletedException
import com.coltsclub.tusa.app.repository.ChatRepository
import com.coltsclub.tusa.app.repository.ChatsActionsRepository
import com.coltsclub.tusa.app.repository.MessageRepository
import com.coltsclub.tusa.app.repository.MessagesActionsRepository
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
    private val chatsActionsRepository: ChatsActionsRepository,
    private val messagesActionsRepository: MessagesActionsRepository,
    private val chatsRepository: ChatRepository,
    private val messageRepository: MessageRepository,
    private val userRepository: UserRepository
) {
    @Transactional
    fun sendMessage(userId: Long, toUserId: Long, message: String, tempId: String): SendMessageResult {
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
                temporaryId = tempId
            )
        )

        // сохраняем действие о отправке сообщения
        messagesActionsRepository.save(
            MessagesActionsEntity(
                messageId = savedMessage.id!!,
                actionType = MessagesActionType.ADD,
                actionTime = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC),
                firstUserId = firstId,
                secondUserId = secondId,
                senderId = userId,
                message = message,
                messageTemporaryId = tempId
            )
        )


        // проверяем наличие чата
        val chatExist = chatsRepository.countChats(userId, toUserId) > 0
        if (!chatExist) {
            val firstUser = userRepository.findById(firstId).getOrNull()?: throw ChatUserDeletedException()
            val secondUser = userRepository.findById(secondId).getOrNull()?: throw ChatUserDeletedException()

            // создаем чат
            val savedChat = chatsRepository.save(
                ChatEntity(
                    firstUserId = firstId,
                    secondUserId = secondId,
                    firsUserName = firstUser.name,
                    secondUserName = secondUser.name,
                    firstUserUniqueName = firstUser.userUniqueName,
                    secondUserUniqueName = secondUser.userUniqueName
                )
            )

            // добавляем действие о создании нового чата
            chatsActionsRepository.save(
                ChatsActionsEntity(
                    chatId = savedChat.id!!,
                    actionType = ChatsActionType.ADD,
                    actionTime = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC),
                    firstUserId = firstId,
                    secondUserId = secondId,
                    firsUserName = savedChat.firsUserName,
                    secondUserName = savedChat.secondUserName,
                    firstUserUniqueName = savedChat.firstUserUniqueName,
                    secondUserUniqueName = savedChat.secondUserUniqueName
                )
            )
            chatCreated = true
        }

        return SendMessageResult(chatCreated)
    }

    fun getChats(userId: Long, pageable: Pageable): Page<ChatEntity> {
        return chatsRepository.findByFirstUserIdOrSecondUserId(userId, userId, pageable)
    }

    fun getActions(userId: Long, actionTime: Long): List<ChatAction> {
        val actions = chatsActionsRepository.findAllByFirstUserIdOrSecondUserIdAndActionTimeGreaterThan(userId, userId, actionTime)
        return actions.map {
            ChatAction(
                chat = ChatResponse(
                    id = it.chatId,
                    firstUserId = it.firstUserId,
                    secondUserId = it.secondUserId,
                    firsUserName = it.firsUserName,
                    secondUserName = it.secondUserName,
                    firstUserUniqueName = it.firstUserUniqueName,
                    secondUserUniqueName = it.secondUserUniqueName
                ),
                actionType = it.actionType,
                actionTime = it.actionTime
            )
        }
    }

    fun getInitChats(id: Long, size: Int): List<ChatResponse> {
        val chats = chatsRepository.findByFirstUserIdOrSecondUserId(id, id, Pageable.unpaged())
        return chats.map {
            ChatResponse(
                id = it.id!!,
                firstUserId = it.firstUserId,
                secondUserId = it.secondUserId,
                firsUserName = it.firsUserName,
                secondUserName = it.secondUserName,
                firstUserUniqueName = it.firstUserUniqueName,
                secondUserUniqueName = it.secondUserUniqueName
            )
        }.toList()
    }

    @Transactional
    fun fullClearChatAndMessages(id: Long, toId: Long) {
        val ids = AlineTwoLongsIds.aline(id, toId)
        val firstId = ids.first
        val secondId = ids.second

        // зачищаем чат если есть
        val chat = chatsRepository.findByFirstUserIdAndSecondUserId(firstId, secondId)
        if (chat != null) {
            // удаляем все действия связанные с чатом
            chatsActionsRepository.deleteAllByFirstUserIdAndSecondUserId(firstId, secondId)

            // сохраняем действие о удалении чата
            // это будет единственное действие о чате в базе
            // когда клиент получит это сообщение он так же очистит все сообщения чата
            chatsActionsRepository.save(
                ChatsActionsEntity(
                    chatId = chat.id!!,
                    actionType = ChatsActionType.DELETE,
                    actionTime = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC),
                    firstUserId = firstId,
                    secondUserId = secondId,
                    firsUserName = chat.firsUserName,
                    secondUserName = chat.secondUserName,
                    firstUserUniqueName = chat.firstUserUniqueName,
                    secondUserUniqueName = chat.secondUserUniqueName
                )
            )

            // удаляем чат
            chatsRepository.delete(chat)
        }

        // зачищаем все сообщения между пользователями
        messageRepository.deleteByFirstUserIdAndSecondUserId(firstId, secondId)
        // зачищаем все действия связанные с сообщениями
        messagesActionsRepository.deleteAllByFirstUserIdAndSecondUserId(firstId, secondId)
    }
}