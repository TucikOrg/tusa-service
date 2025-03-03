package com.coltsclub.tusa.app.repository

import com.coltsclub.tusa.app.entity.FriendRequestEntity
import java.util.Optional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface FriendRequestRepository : JpaRepository<FriendRequestEntity, Long> {
    @Query("SELECT f FROM friendRequest f WHERE (f.firstUserId = :firstUserId OR f.secondUserId = :secondUserId) AND f.updateTime > :updateTime")
    fun findAllByFirstUserIdOrSecondUserIdAndUpdateTimeGreaterThan(
        firstUserId: Long,
        secondUserId: Long,
        updateTime: Long
    ): List<FriendRequestEntity>

    fun findByFirstUserIdAndSecondUserIdAndDeleted(
        firstUserId: kotlin.Long, secondUserId: kotlin.Long, deleted: kotlin.Boolean
    ) : FriendRequestEntity?

    @Query("SELECT f FROM friendRequest f WHERE (f.firstUserId = :firstUserId OR f.secondUserId = :secondUserId) AND f.deleted = :deleted")
    fun findByFirstUserIdOrSecondUserIdAndDeleted(firstUserId: kotlin.Long, secondUserId: kotlin.Long, deleted: kotlin.Boolean) : List<FriendRequestEntity>
}