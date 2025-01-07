package com.coltsclub.tusa.app.service

import com.coltsclub.tusa.app.dto.SendMessage
import com.coltsclub.tusa.app.dto.SendMessageResult
import com.coltsclub.tusa.app.dto.SetMuteState
import com.coltsclub.tusa.app.entity.ChatEntity
import com.coltsclub.tusa.app.entity.MessageEntity
import com.coltsclub.tusa.app.repository.ChatRepository
import com.coltsclub.tusa.app.repository.MessageRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class ChatsService(
    private val chatsRepository: ChatRepository,
    private val messageRepository: MessageRepository
) {
    fun setMuteStateChat(setMuteState: SetMuteState, owner: Long): ChatEntity? {
        val chat = chatsRepository.findByToIdAndOwnerId(toId = setMuteState.toId, ownerId = owner) ?: return null
        chat.muted = setMuteState.state
        return chatsRepository.save(chat)
    }

    fun deleteChat(toId: Long, owner: Long): ChatEntity? {
        val chat = chatsRepository.findByToIdAndOwnerId(toId = toId, ownerId = owner) ?: return null
        if (chat.ownerId != owner) return null
        return chatsRepository.save(chat.apply {
            deleted = true
        })
    }

    fun sendMessage(sendMessage: SendMessage, owner: Long): SendMessageResult {
        var chats = emptyList<ChatEntity>()
        var chatsNew = false
        val chatOne = chatsRepository.findByToIdAndOwnerId(toId = sendMessage.toId, ownerId = owner)
        if (chatOne == null) {
            chatsNew = true
            chats = initiateNewCommunication(owner, sendMessage.toId, sendMessage.message)
        } else {
            val chatSecond = chatsRepository.findByToIdAndOwnerId(toId = owner, ownerId = sendMessage.toId)!!
            chats = listOf(chatOne, chatSecond)
        }

        val result = messageRepository.save(
            MessageEntity(
                ownerId = owner,
                toId = sendMessage.toId,
                payload = sendMessage.payload,
                deletedOwner = false,
                deletedTo = false,
                changed = false,
                read = false,
                chatId = chats.first().chatId,
                message = sendMessage.message
            )
        )

        for (chatEl in chats) {
            chatEl.lastMessage = sendMessage.message
            chatEl.lastMessageOwner = owner
            chatsRepository.save(chatEl)
        }

        return SendMessageResult(
            chatsNew = chatsNew,
            newChats = chats,
            messageEntity = result
        )
    }

    fun initiateNewCommunication(senderMessageId: Long, receiverId: Long, message: String): List<ChatEntity> {
        val chatId = (messageRepository.findMaxChatId()?: 0) + 1
        val chatFirst = ChatEntity(
            ownerId = senderMessageId,
            toId = receiverId,
            muted = false,
            chatId = chatId,
            lastMessage = message,
            lastMessageOwner = senderMessageId
        )
        val chatSecond = ChatEntity(
            ownerId = receiverId,
            toId = senderMessageId,
            muted = false,
            chatId = chatId,
            lastMessage = message,
            lastMessageOwner = senderMessageId
        )
        val result = chatsRepository.saveAll(listOf(chatFirst, chatSecond)).toList()
        return result
    }

    fun findChat(id: Long, toId: Long): ChatEntity? {
        return chatsRepository.findByToIdAndOwnerId(toId = toId, ownerId = id)
    }

    fun getChats(ownerId: Long, pageable: Pageable): Page<ChatEntity> {
        return chatsRepository.findAllByOwnerId(ownerId, pageable)
    }
}