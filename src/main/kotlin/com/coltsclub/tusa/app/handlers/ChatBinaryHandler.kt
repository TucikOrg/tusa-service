package com.coltsclub.tusa.app.handlers

import com.coltsclub.tusa.app.dto.ChatResponse
import com.coltsclub.tusa.app.dto.ChatsResponse
import com.coltsclub.tusa.app.dto.MessageResponse
import com.coltsclub.tusa.app.dto.RequestChats
import com.coltsclub.tusa.app.dto.RequestMessages
import com.coltsclub.tusa.app.dto.ResponseMessages
import com.coltsclub.tusa.app.dto.SendMessage
import com.coltsclub.tusa.app.dto.messenger.InitChatsResponse
import com.coltsclub.tusa.app.dto.messenger.InitMessenger
import com.coltsclub.tusa.app.dto.messenger.InitMessagesResponse
import com.coltsclub.tusa.app.exceptions.ChatUserDeletedException
import com.coltsclub.tusa.app.service.ChatsService
import com.coltsclub.tusa.app.service.MessagesService
import com.coltsclub.tusa.core.entity.UserEntity
import com.coltsclub.tusa.core.socket.SocketBinaryMessage
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.web.socket.BinaryMessage
import org.springframework.web.socket.WebSocketSession

@Service
class ChatBinaryHandler(
    private val messagesService: MessagesService,
    private val chatService: ChatsService
) {
    lateinit var sendToSessionsOf: (Long, BinaryMessage) -> Unit

    @OptIn(ExperimentalSerializationApi::class)
    fun handleBinaryMessage(
        socketMessage: SocketBinaryMessage,
        user: UserEntity,
        session: WebSocketSession
    ) {
        when (socketMessage.type) {
            "chats-actions" -> {
                // получаем историю событий для и пользователя
                val appTimePoint = Cbor.decodeFromByteArray<Long>(socketMessage.data)
                val actions = chatService.getActions(
                    userId = user.id!!,
                    actionTime = appTimePoint
                )
                // отправляем историю действий запросившему пользователю
                val data = Cbor.encodeToByteArray(actions)
                val actionsBinary = BinaryMessage(Cbor.encodeToByteArray(SocketBinaryMessage("chats-actions", data)))
                session.sendMessage(actionsBinary)
            }
            "messages-actions" -> {
                // получаем историю с сообщениями для и пользователя
                val appTimePoint = Cbor.decodeFromByteArray<Long>(socketMessage.data)
                val actions = messagesService.getActions(
                    userId = user.id!!,
                    actionTime = appTimePoint
                )

                // отправляем историю действий запросившему пользователю
                val data = Cbor.encodeToByteArray(actions)
                val actionsBinary = BinaryMessage(Cbor.encodeToByteArray(SocketBinaryMessage("messages-actions", data)))
                session.sendMessage(actionsBinary)
            }
            "init-messages" -> {
                val initMessengerRequest = Cbor.decodeFromByteArray<InitMessenger>(socketMessage.data)

                // получаем самые актуальные 50 сообщений каждого чата
                val messages = messagesService.getInitMessages(user.id!!, initMessengerRequest.size)
                val data = Cbor.encodeToByteArray(
                    InitMessagesResponse(
                        messages = messages,
                        timePoint = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
                    )
                )

                val initMessagesBinary = BinaryMessage(Cbor.encodeToByteArray(SocketBinaryMessage("init-messages", data)))
                session.sendMessage(initMessagesBinary)
            }
            "init-chats" -> {
                val initMessengerRequest = Cbor.decodeFromByteArray<InitMessenger>(socketMessage.data)

                // получаем самые актуальные 50 сообщений каждого чата
                val chats = chatService.getInitChats(user.id!!, initMessengerRequest.size)
                val data = Cbor.encodeToByteArray(
                    InitChatsResponse(
                        chats = chats,
                        timePoint = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
                    )
                )

                val initMessagesBinary = BinaryMessage(Cbor.encodeToByteArray(SocketBinaryMessage("init-chats", data)))
                session.sendMessage(initMessagesBinary)
            }
            "messages" -> {
                // получаем страницу сообщений
                val requestMessages = Cbor.decodeFromByteArray<RequestMessages>(socketMessage.data)
                val pageable = PageRequest.of(requestMessages.page, requestMessages.size, Sort.by(Sort.Direction.DESC, "creation"))
                val messages = messagesService.getMessages(
                    userId = user.id!!,
                    withUserId = requestMessages.withUserId,
                    pageable = pageable
                )
                // преобразуем сообщения в ответ dto
                val responseMessages = messages.map {
                    MessageResponse(
                        id = it.id,
                        firstUserId = it.firstUserId,
                        secondUserId = it.secondUserId,
                        senderId = it.senderId,
                        message = it.message,
                        creation = it.creation.toEpochSecond(ZoneOffset.UTC),
                        temporaryId = it.temporaryId
                    )
                }
                val data = Cbor.encodeToByteArray(
                    ResponseMessages(
                        messages = responseMessages.toList(),
                        totalPages = messages.totalPages,
                        page = requestMessages.page
                    )
                )
                // отправляем ответ запросившему пользователю
                val response = Cbor.encodeToByteArray(SocketBinaryMessage("messages", data))
                session.sendMessage(BinaryMessage(response))
            }
            "chats" -> {
                // ищем созданные чаты пользователя
                val requestChats = Cbor.decodeFromByteArray<RequestChats>(socketMessage.data)
                val pageable = PageRequest.of(requestChats.page, requestChats.size)
                val chats = chatService.getChats(user.id!!, pageable)

                // преобразуем чаты в ответ dto
                val chatsDto = chats.map { chat ->
                    ChatResponse(
                        id = chat.id!!,
                        firstUserId = chat.firstUserId,
                        secondUserId = chat.secondUserId,
                        firsUserName = chat.firsUserName,
                        secondUserName = chat.secondUserName,
                        firstUserUniqueName = chat.firstUserUniqueName,
                        secondUserUniqueName = chat.secondUserUniqueName
                    )
                }
                val chatsResponse = ChatsResponse(
                    chats = chatsDto.toList(),
                    totalPages = chats.totalPages,
                    page = requestChats.page
                )

                // отправляем ответ запросившему пользователю
                val data = Cbor.encodeToByteArray(chatsResponse)
                val response = Cbor.encodeToByteArray(SocketBinaryMessage("chats", data))
                session.sendMessage(BinaryMessage(response))
            }
            "send-message" -> {
                // отправляем сообщение
                val sendMessage = Cbor.decodeFromByteArray<SendMessage>(socketMessage.data)
                try {
                    val result = chatService.sendMessage(
                        userId = user.id!!,
                        toUserId = sendMessage.toId,
                        message = sendMessage.message,
                        tempId = sendMessage.temporaryId
                    )

                    // уведомляем пользователя о новом сообщении
                    // и уведомляем отправителя что сообщение было доставлено
                    val refreshMessages = BinaryMessage(Cbor.encodeToByteArray(SocketBinaryMessage("refresh-messages", byteArrayOf())))
                    sendToSessionsOf(sendMessage.toId, refreshMessages)
                    sendToSessionsOf(user.id, refreshMessages)

                    // если чат был создан то уведомляем о том что нужно обновить состояние чатов
                    if (result.chatCreated) {
                        val refreshChats = BinaryMessage(Cbor.encodeToByteArray(SocketBinaryMessage("refresh-chats", byteArrayOf())))
                        sendToSessionsOf(sendMessage.toId, refreshChats)
                        sendToSessionsOf(user.id, refreshChats)
                    }
                } catch (e: ChatUserDeletedException) {
                    // если например пользователь удален, а у первого остался чат то вылезет исключение
                    // полностью в базе зачищаем чат
                    chatService.fullClearChatAndMessages(user.id!!, sendMessage.toId)

                    // сообщаем тому кто пытался отправить сообщение что чат был зачищен
                    val refreshChats = BinaryMessage(Cbor.encodeToByteArray(SocketBinaryMessage("refresh-chats", byteArrayOf())))
                    sendToSessionsOf(user.id, refreshChats)
                }
            }
        }
    }
}