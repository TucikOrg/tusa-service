package com.coltsclub.tusa.app.controller

import com.coltsclub.tusa.app.entity.AvatarActionType
import com.coltsclub.tusa.app.entity.AvatarActionsEntity
import com.coltsclub.tusa.app.entity.AvatarEntity
import com.coltsclub.tusa.app.entity.ImageEntity
import com.coltsclub.tusa.app.repository.ImageRepository
import com.coltsclub.tusa.core.exceptions.TucikBadRequest
import java.time.LocalDateTime
import java.time.ZoneOffset
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
class ImageController(
    private val imageRepository: ImageRepository
) {

    @PostMapping("api/v1/image", consumes = ["multipart/form-data"])
    @PreAuthorize("hasRole('USER')")
    fun addImage(file: MultipartFile, fileId: String): String {
        val userId = SecurityContextHolder.getContext().authentication.name.toLong()
        return saveImage(file, userId, fileId).toString()
    }

    private fun saveImage(file: MultipartFile, userId: Long, fileId: String): Long {
        if (file.isEmpty) {
            throw TucikBadRequest("File is empty")
        }

        // cохраняем фотку
        val entity = ImageEntity(
            ownerId = userId,
            image = file.bytes,
            localFilePathId = fileId,
        )
        val saved = imageRepository.save(entity)
        return saved.id!!
    }
}