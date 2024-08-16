package com.coltsclub.tusa.app.controller

import com.coltsclub.tusa.app.entity.AvatarEntity
import com.coltsclub.tusa.app.repository.AvatarRepository
import java.util.Optional
import kotlin.jvm.optionals.getOrNull
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
class AvatarController(
    val avatarRepository: AvatarRepository
) {

    @PostMapping("/avatar", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun addAvatar(file: MultipartFile, owner: String) {
        val entity = AvatarEntity(
            owner = owner,
            avatar = file.bytes
        )
        avatarRepository.save(entity)
    }

    @GetMapping("/avatar")
    fun getAvatars(owner: String): List<AvatarEntity> {
        return avatarRepository.findAllByOwner(owner)
    }

    @GetMapping("/avatar/image")
    fun getAvatarImage(owner: String): ResponseEntity<ByteArray> {
        val avatar = avatarRepository.findLatestByOwner(owner).getOrNull()?.avatar ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(avatar)
    }
}