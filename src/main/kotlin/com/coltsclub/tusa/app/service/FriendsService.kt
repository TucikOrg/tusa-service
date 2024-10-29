package com.coltsclub.tusa.app.service

import com.coltsclub.tusa.app.dto.FriendDto
import com.coltsclub.tusa.app.dto.FriendRequestDto
import com.coltsclub.tusa.app.entity.FriendEntity
import com.coltsclub.tusa.app.entity.FriendRequestEntity
import com.coltsclub.tusa.app.repository.FriendRepository
import com.coltsclub.tusa.app.repository.FriendRequestRepository
import com.coltsclub.tusa.core.repository.UserRepository
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class FriendsService(
    private val friendRepository: FriendRepository,
    private val friendRequestRepository: FriendRequestRepository,
    private val userRepository: UserRepository
) {
    fun getFriends(userId: Long): List<FriendDto> {
        val friends = friendRepository.findByFromUserId(userId)
        val users = userRepository.findAllById(friends.map { it.toUserId }).toList()
        return users.map { user ->
            FriendDto(user.id, user.name, user.userUniqueName)
        }
    }

    fun filterFriends(userId: Long, users: List<Long>): List<Long> {
        return friendRepository.findAllByFromUserIdAndToUserIdIn(userId, users).map { it.toUserId }
    }

    fun addFriend(from: Long, requestTo: Long): FriendRequestEntity {
        val friendRequestOpt = friendRequestRepository.findFriendRequest(from, requestTo)
        if (!friendRequestOpt.isPresent) {
            val friendRequest = friendRequestRepository.save(FriendRequestEntity(from, requestTo))
            return friendRequest
        }

        return friendRequestOpt.get()
    }

    fun deleteFriend(userId: Long, deleteFriend: Long) {
        friendRepository.deleteByFromUserIdAndToUserId(userId, deleteFriend)
        friendRepository.deleteByFromUserIdAndToUserId(deleteFriend, userId)
    }

    fun acceptFriend(userId: Long, requestFrom: Long): FriendDto {
        val friendRequest = friendRequestRepository.findFriendRequest(requestFrom, userId)
        if (friendRequest.isEmpty) {
            throw Exception("Friend request not found")
        }
        friendRequestRepository.delete(friendRequest.get())

        val requestSecond = friendRequestRepository.findFriendRequest(userId, requestFrom)
        if (requestSecond.isPresent) {
            friendRequestRepository.delete(requestSecond.get())
        }

        val save = listOf(FriendEntity(userId, requestFrom), FriendEntity(requestFrom, userId))
        friendRepository.saveAll(save)

        val userOpt = userRepository.findById(requestFrom)
        if (userOpt.isEmpty) {
            throw Exception("User not found")
        }
        val user = userOpt.get()
        return FriendDto(user.id, user.name, user.userUniqueName)
    }

    fun getToMeRequests(userId: Long): List<FriendRequestDto> {
        val requests = friendRequestRepository.findToUserRequests(userId)
        val users = userRepository.findAllById(requests.map { it.fromUserId }).toList()
        return users.map { user ->
            FriendRequestDto(user.id, user.name, user.userUniqueName)
        }
    }

    fun deleteRequest(userId: Long, requestFrom: Long) {
        friendRequestRepository.deleteByFromUserIdAndToUserId(requestFrom, userId)
    }

    fun findUsers(userId: Long, name: String): List<FriendDto> {
        val users = userRepository.findCandidateFriends(name, userId, Pageable.ofSize(10))
        return users.map { user ->
            FriendDto(user.id, user.name, user.userUniqueName)
        }
    }
}