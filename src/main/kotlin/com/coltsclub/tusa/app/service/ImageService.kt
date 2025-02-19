package com.coltsclub.tusa.app.service

import com.coltsclub.tusa.app.repository.AvatarRepository
import com.coltsclub.tusa.app.repository.ImageRepository
import kotlin.jvm.optionals.getOrNull
import org.springframework.stereotype.Service

@Service
class ImageService(
    private val imageRepository: ImageRepository
) {
    fun getImage(id: Long): ByteArray? {
        return imageRepository.findById(id).getOrNull()?.image
    }

    fun getImageByTempFileId(tempFileId: String, owner: Long): ByteArray? {
        return imageRepository.findByOwnerIdAndLocalFilePathId(owner, tempFileId)?.image
    }
}