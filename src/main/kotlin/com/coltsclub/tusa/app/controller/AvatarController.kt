package com.coltsclub.tusa.app.controller

import com.coltsclub.tusa.app.entity.AvatarEntity
import com.coltsclub.tusa.app.repository.AvatarRepository
import kotlin.jvm.optionals.getOrNull
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
class AvatarController(
    val avatarRepository: AvatarRepository
) {
    @PostMapping("api/v1/avatar", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun addAvatar(file: MultipartFile, owner: String) {
        val context = SecurityContextHolder.getContext()
        var phone: String? = null
        if (context != null && context.authentication != null)
            phone = context.authentication.name

        val entity = AvatarEntity(
            owner = owner,
            avatar = file.bytes,
            phone = phone
        )
        avatarRepository.save(entity)
    }

    @GetMapping("api/v1/avatar")
    fun getAvatars(owner: String): List<AvatarEntity> {
        return avatarRepository.findAllByOwner(owner)
    }

    @GetMapping("api/v1/avatar/image")
    fun getAvatarImage(owner: String): ResponseEntity<ByteArray> {
        val avatar = avatarRepository.findLatestByOwner(owner).getOrNull()?.avatar ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(avatar)
    }

    @PostMapping("api/v1/avatar/link-profile")
    @PreAuthorize("hasRole('USER')")
    fun linkAvatars(@RequestBody noLoginId: String) {
        val phone = SecurityContextHolder.getContext().authentication.name
        val avatars = avatarRepository.findAllByOwner(noLoginId).filter { it.phone == null }
        avatars.forEach { it.phone = phone }
        if (avatars.isNotEmpty())
            avatarRepository.saveAll(avatars)
    }
}