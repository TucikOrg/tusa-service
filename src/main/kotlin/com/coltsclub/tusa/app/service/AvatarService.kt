package com.coltsclub.tusa.app.service

import com.coltsclub.tusa.app.repository.AvatarRepository
import kotlin.jvm.optionals.getOrNull
import org.springframework.stereotype.Service

@Service
class AvatarService(
    private val avatarRepository: AvatarRepository
) {
    fun getAvatarImage(userId: Long): ByteArray? {
        return avatarRepository.findLatestByOwnerId(userId).getOrNull()?.avatar
    }
}