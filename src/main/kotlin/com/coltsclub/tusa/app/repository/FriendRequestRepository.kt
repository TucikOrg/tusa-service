package com.coltsclub.tusa.app.repository

import com.coltsclub.tusa.app.entity.FriendRequestEntity
import java.util.Optional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface FriendRequestRepository : JpaRepository<FriendRequestEntity, Long> {
    @Query(
        value = """
          select f from friendRequest f 
          where f.fromUserId = :fromUserId and f.toUserId = :toUserId
        """
    )
    fun findFriendRequest(fromUserId: Long, toUserId: Long): Optional<FriendRequestEntity>

    @Query(
        value = """
          select f from friendRequest f 
          where f.toUserId = :toUserId
        """
    )
    fun findToUserRequests(toUserId: Long): List<FriendRequestEntity>

    fun deleteByFromUserIdAndToUserId(requestFrom: Long, userId: Long)
}