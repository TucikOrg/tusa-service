package com.coltsclub.tusa.core.service

import com.coltsclub.tusa.core.repository.UserRepository
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import kotlin.jvm.optionals.getOrNull
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class PushNotificationService(
    private val userRepository: UserRepository
) {
    private val logger = LoggerFactory.getLogger(PushNotificationService::class.java)
    fun sendPushNotification(token: String, title: String, body: String) {
        try {
            val message = Message.builder()
                .setToken(token)
                .putData("title", title) // Передаём title как данные
                .putData("body", body)   // Передаём body как данные
                .build()

            FirebaseMessaging.getInstance().send(message)
        } catch (e: Exception) {
            logger.error("Firebase sending new message notification error", e)
        }

    }

    fun sendNewMessageNotification(toUserId: Long, fromUserId: Long, message: String) {
        val toUser = userRepository.findById(toUserId).getOrNull()?: return
        val firebaseToken = toUser.firebaseToken?: return
        if (firebaseToken.isEmpty() || firebaseToken.isBlank()) return
        val fromUser = userRepository.findById(fromUserId).getOrNull()?: return
        sendPushNotification(firebaseToken, fromUser.name, message)
    }
}