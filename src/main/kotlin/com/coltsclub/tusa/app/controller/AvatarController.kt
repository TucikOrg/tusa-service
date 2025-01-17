package com.coltsclub.tusa.app.controller

import com.coltsclub.tusa.app.entity.AvatarActionType
import com.coltsclub.tusa.app.entity.AvatarActionsEntity
import com.coltsclub.tusa.app.entity.AvatarEntity
import com.coltsclub.tusa.app.repository.AvatarActionsRepository
import com.coltsclub.tusa.app.repository.AvatarRepository
import com.coltsclub.tusa.core.exceptions.TucikBadRequest
import com.coltsclub.tusa.core.socket.WebSocketHandler
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
class AvatarController(
    val avatarRepository: AvatarRepository,
    val avatarActionsRepository: AvatarActionsRepository,
    val webSocketHandler: WebSocketHandler
) {
    @PostMapping("api/v1/avatar", consumes = ["multipart/form-data"])
    @PreAuthorize("hasRole('USER')")
    fun addAvatar(file: MultipartFile) {
        val userId = SecurityContextHolder.getContext().authentication.name.toLong()
        saveAvatar(file, userId)
    }

    @PostMapping("api/v1/avatar/{userId}", consumes = ["multipart/form-data"])
    @PreAuthorize("hasRole('USER')")
    fun addAvatar(file: MultipartFile, @PathVariable userId: Long) {
        val initiatorUserId = SecurityContextHolder.getContext().authentication.name.toLong()
        saveAvatar(file, userId)
    }

    private fun saveAvatar(file: MultipartFile, userId: Long) {
        if (file.isEmpty) {
            throw TucikBadRequest("File is empty")
        }

        // cохраняем аватарку
        val entity = AvatarEntity(
            ownerId = userId,
            avatar = file.bytes,
        )
        avatarRepository.save(entity)

        // сохраняем действие смены аватарки
        avatarActionsRepository.save(
            AvatarActionsEntity(
                ownerId = userId,
                actionType = AvatarActionType.CHANGE,
                actionTime = System.currentTimeMillis()
            )
        )

        webSocketHandler.sendToFriendsAvatarUpdated(userId)
    }
}