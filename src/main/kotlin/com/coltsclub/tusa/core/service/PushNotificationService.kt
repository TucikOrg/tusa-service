package com.coltsclub.tusa.core.service

import com.coltsclub.tusa.core.repository.UserRepository
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import kotlin.jvm.optionals.getOrNull
import org.springframework.stereotype.Service

@Service
class PushNotificationService(
    private val userRepository: UserRepository
) {
    fun sendPushNotification(token: String, title: String, body: String) {
        val message = Message.builder()
            .setToken(token)
            .setNotification(Notification.builder().setTitle(title).setBody(body).build())
            .build()

        FirebaseMessaging.getInstance().send(message)
    }

    fun sendNewMessageNotification(toUserId: Long, fromUserId: Long, message: String) {
        val toUser = userRepository.findById(toUserId).getOrNull()?: return
        val firebaseToken = toUser.firebaseToken?: return
        if (firebaseToken.isEmpty() || firebaseToken.isBlank()) return
        val fromUser = userRepository.findById(fromUserId).getOrNull()?: return
        sendPushNotification(firebaseToken, fromUser.name, message)
    }
}