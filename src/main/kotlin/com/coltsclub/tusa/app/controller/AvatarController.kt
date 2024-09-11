package com.coltsclub.tusa.app.controller

import com.coltsclub.tusa.app.entity.AvatarEntity
import com.coltsclub.tusa.app.repository.AvatarRepository
import com.coltsclub.tusa.core.exceptions.TucikBadRequest
import kotlin.jvm.optionals.getOrNull
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
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
        val context = SecurityContextHolder.getContext()
        val phone = context.authentication.name
        if (file.isEmpty) {
            throw TucikBadRequest("File is empty")
        }

        val entity = AvatarEntity(
            phone = phone,
            avatar = file.bytes,
        )
        avatarRepository.save(entity)
    }

    @GetMapping("api/v1/avatar")
    fun getAvatars(phone: String): List<AvatarEntity> {
        return avatarRepository.findAllByPhone(phone)
    }

    @GetMapping("api/v1/avatar/image")
    fun getAvatarImage(phone: String): ResponseEntity<ByteArray> {
        val avatar = avatarRepository.findLatestByPhone(phone).getOrNull()?.avatar ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(avatar)
    }
}