package com.coltsclub.tusa.app.repository

import com.coltsclub.tusa.app.entity.ChatEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ChatRepository: CrudRepository<ChatEntity, Long> {
    @Query("SELECT COUNT(c) FROM chat c WHERE (c.firstUserId = :firstUserId AND c.secondUserId = :secondUserId) OR (c.firstUserId = :secondUserId AND c.secondUserId = :firstUserId)")
    fun countChats(firstUserId: Long, secondUserId: Long): Long
    fun findByFirstUserIdAndSecondUserId(firstId: Long, secondId: Long): ChatEntity?

    @Query("SELECT c FROM chat c WHERE (c.firstUserId = :firstUserId OR c.secondUserId = :secondUserId) AND c.updateTime > :updateTime")
    fun findAllByFirstUserIdOrSecondUserIdAndUpdateTimeGreaterThan(
        firstUserId: Long,
        secondUserId: Long,
        updateTime: Long
    ): List<ChatEntity>

    @Query("SELECT c FROM chat c WHERE (c.firstUserId = :firstUserId OR c.secondUserId = :secondUserId) AND c.deleted = :deleted")
    fun findByFirstUserIdOrSecondUserIdAndDeleted(
        firstUserId: kotlin.Long,
        secondUserId: kotlin.Long,
        pageable: org.springframework.data.domain.Pageable,
        deleted: kotlin.Boolean
    ): Page<ChatEntity>
}