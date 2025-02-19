package com.coltsclub.tusa.app.repository

import com.coltsclub.tusa.app.entity.ImageEntity
import org.springframework.data.repository.CrudRepository

interface ImageRepository: CrudRepository<ImageEntity, Long> {

    fun findByOwnerIdAndLocalFilePathId(ownerId: Long, localFilePathId: String): ImageEntity?
}