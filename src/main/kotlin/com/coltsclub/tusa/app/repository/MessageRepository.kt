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
    fun findByFirstUserIdAndSecondUserId(firstUserId: Long, secondUserId: Long, pageable: Pageable): Page<MessageEntity>

    @Query(
        value = """
        SELECT * FROM (
            SELECT *, ROW_NUMBER() OVER (PARTITION BY LEAST(first_user_id, second_user_id), GREATEST(first_user_id, second_user_id) ORDER BY creation DESC) AS row_num
            FROM message
            WHERE :userId IN (first_user_id, second_user_id)
        ) subquery
        WHERE row_num <= :size
        """,
        nativeQuery = true
    )
    fun findTopPerUserGroup(@Param("userId") userId: Long, @Param("size") size: Int): List<MessageEntity>

    fun deleteByFirstUserIdAndSecondUserId(firstId: Long, secondId: Long)
}