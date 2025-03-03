package com.coltsclub.tusa.app.service

import com.coltsclub.tusa.app.dto.AvatarForCheck
import com.coltsclub.tusa.app.entity.AvatarEntity
import com.coltsclub.tusa.app.repository.AvatarRepository
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.jvm.optionals.getOrNull
import org.springframework.stereotype.Service

@Service
class AvatarService(
    private val avatarRepository: AvatarRepository
) {
    fun getAvatarImage(userId: Long): ByteArray? {
        return avatarRepository.findLatestByOwnerId(userId).getOrNull()?.avatar
    }

    fun getUpdatedAvatars(avatarsForCheck: List<AvatarForCheck>): List<AvatarEntity> {
        return avatarsForCheck.mapNotNull { avatarCheck ->
            avatarRepository.findByOwnerIdAndCreationGreaterThan(
                avatarCheck.ownerId,
                avatarCheck.updatingTime
            )?.maxByOrNull { it.creation }
        }
    }
}