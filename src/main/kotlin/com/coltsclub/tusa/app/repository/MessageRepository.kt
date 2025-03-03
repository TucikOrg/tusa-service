package com.coltsclub.tusa.app.repository

import com.coltsclub.tusa.app.entity.MessageEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface MessageRepository: CrudRepository<MessageEntity, Long> {
    fun findByFirstUserIdAndSecondUserIdAndDeleted(firstUserId: Long, secondUserId: Long, pageable: Pageable, deleted: Boolean): Page<MessageEntity>

    @Query(
        value = """
        SELECT * FROM (
            SELECT *, ROW_NUMBER() OVER (PARTITION BY LEAST(first_user_id, second_user_id), GREATEST(first_user_id, second_user_id) ORDER BY update_time DESC) AS row_num
            FROM message
            WHERE :userId IN (first_user_id, second_user_id) and deleted = false
        ) subquery
        WHERE row_num <= :size
        """,
        nativeQuery = true
    )
    fun findTopPerUserGroup(@Param("userId") userId: Long, @Param("size") size: Int): List<MessageEntity>

    fun deleteByFirstUserIdAndSecondUserId(firstId: Long, secondId: Long)

    @Query("SELECT m FROM message m WHERE (m.firstUserId = :firstUserId OR m.secondUserId = :secondUserId) AND m.updateTime > :updateTime")
    fun findAllByFirstUserIdOrSecondUserIdAndUpdateTimeGreaterThan(
        firstUserId: Long,
        secondUserId: Long,
        updateTime: Long
    ): List<MessageEntity>
}