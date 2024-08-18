package com.coltsclub.tusa.app.controller

import com.coltsclub.tusa.app.entity.FriendEntity
import com.coltsclub.tusa.app.entity.FriendRequestEntity
import com.coltsclub.tusa.app.repository.FriendRepository
import com.coltsclub.tusa.app.repository.FriendRequestRepository
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
@PreAuthorize("hasRole('USER')")
class FriendController(
    private val friendRequestRepository: FriendRequestRepository,
    private val friendRepository: FriendRepository
) {
    @PostMapping("api/v1/friend/add")
    fun addFriend(requestTo: String): ResponseEntity<FriendRequestEntity> {
        val from = SecurityContextHolder.getContext().authentication.name
        val friendRequest = friendRequestRepository.findActiveFriendRequest(from, requestTo)
        if (friendRequest.isPresent) {
            return ResponseEntity.badRequest().build()
        }

        val sentRequest = friendRequestRepository.save(FriendRequestEntity(from, requestTo))
        return ResponseEntity.ok(sentRequest)
    }

    @PostMapping("api/v1/friend/accept")
    fun acceptFriend(requestFrom: String): ResponseEntity<FriendRequestEntity> {
        val to = SecurityContextHolder.getContext().authentication.name
        val friendRequest = friendRequestRepository.findActiveFriendRequest(requestFrom, to)
        if (friendRequest.isEmpty) {
            return ResponseEntity.badRequest().build()
        }

        val acceptedRequest = friendRequest.get().copy(activated = true)
        friendRequestRepository.save(acceptedRequest)

        val save = listOf(FriendEntity(to, requestFrom), FriendEntity(requestFrom, to))
        friendRepository.saveAll(save)

        return ResponseEntity.ok(acceptedRequest)
    }

    @DeleteMapping("api/v1/friend")
    fun deleteFriend(@RequestBody deleteFriend: String): ResponseEntity<Void> {
        val phone = SecurityContextHolder.getContext().authentication.name
        friendRepository.deleteByOwnerAndHasFriend(phone, deleteFriend)
        friendRepository.deleteByOwnerAndHasFriend(deleteFriend, phone)
        return ResponseEntity.ok().build()
    }

    @GetMapping("api/v1/friend/my")
    fun getMyFriends(): List<FriendEntity> {
        val phone = SecurityContextHolder.getContext().authentication.name
        return friendRepository.findByOwner(phone)
    }

    @GetMapping("api/v1/friend/requests")
    fun getToMeRequests(): List<FriendRequestEntity> {
        val phone = SecurityContextHolder.getContext().authentication.name
        return friendRequestRepository.findToMeRequests(phone)
    }
}