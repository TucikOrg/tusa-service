package com.coltsclub.tusa.app.dto

import com.coltsclub.tusa.app.entity.ChatEntity
import com.coltsclub.tusa.app.entity.MessageEntity

class SendMessageResult(
    val chatsNew: Boolean,
    val newChats: List<ChatEntity>,
    val messageEntity: MessageEntity
) {
}