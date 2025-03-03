package com.coltsclub.tusa.app.repository

import com.coltsclub.tusa.app.entity.FriendEntity
import java.util.Optional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface FriendRepository : JpaRepository<FriendEntity, Long> {
    @Query("SELECT f FROM friend f WHERE (f.firstUserId = :firstUserId OR f.secondUserId = :secondUserId) AND f.updateTime > :updateTime")
    fun findAllByFirstUserIdOrSecondUserIdAndUpdateTimeGreaterThan(
        firstUserId: Long,
        secondUserId: Long,
        updateTime: Long
    ): List<FriendEntity>

    fun findByFirstUserIdAndSecondUserIdAndDeleted(firstUserId: Long, secondUserId: Long, deleted: Boolean): FriendEntity?

    @Query("SELECT f FROM friend f WHERE (f.firstUserId = :firstUserId OR f.secondUserId = :secondUserId) AND f.deleted = :deleted")
    fun findByFirstUserIdOrSecondUserIdAndDeleted(firstUserId: Long, secondUserId: Long, deleted: Boolean): List<FriendEntity>
}