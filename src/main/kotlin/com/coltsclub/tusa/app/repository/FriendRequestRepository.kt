package com.coltsclub.tusa.app.repository

import com.coltsclub.tusa.app.entity.FriendRequestEntity
import java.util.Optional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface FriendRequestRepository : JpaRepository<FriendRequestEntity, Long> {
    @Query(
        value = """
          select f from friendRequest f 
          where f.fromPhone = :fromPhone and f.toPhone = :toPhone and f.activated = false
        """
    )
    fun findActiveFriendRequest(fromPhone: String, toPhone: String): Optional<FriendRequestEntity>

    @Query(
        value = """
          select f from friendRequest f 
          where f.toPhone = :toPhone and f.activated = false
        """
    )
    fun findToMeRequests(toPhone: String): List<FriendRequestEntity>
}