package com.coltsclub.tusa.app.controller

import com.coltsclub.tusa.app.entity.AvatarEntity
import com.coltsclub.tusa.app.repository.AvatarRepository
import com.coltsclub.tusa.core.exceptions.TucikBadRequest
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
class AvatarController(
    val avatarRepository: AvatarRepository
) {
    @PostMapping("api/v1/avatar", consumes = ["multipart/form-data"])
    @PreAuthorize("hasRole('USER')")
    fun addAvatar(file: MultipartFile) {
        val userId = SecurityContextHolder.getContext().authentication.name.toLong()
        if (file.isEmpty) {
            throw TucikBadRequest("File is empty")
        }

        val entity = AvatarEntity(
            ownerId = userId,
            avatar = file.bytes,
        )
        avatarRepository.save(entity)
    }

    @PostMapping("api/v1/avatar/{userId}", consumes = ["multipart/form-data"])
    @PreAuthorize("hasRole('USER')")
    fun addAvatar(file: MultipartFile, @PathVariable userId: Long) {
        val initiatorUserId = SecurityContextHolder.getContext().authentication.name.toLong()
        if (file.isEmpty) {
            throw TucikBadRequest("File is empty")
        }

        val entity = AvatarEntity(
            ownerId = userId,
            avatar = file.bytes,
        )
        avatarRepository.save(entity)
    }
}