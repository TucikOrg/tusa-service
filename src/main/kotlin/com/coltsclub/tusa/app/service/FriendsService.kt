package com.coltsclub.tusa.app.service

import com.coltsclub.tusa.app.dto.FriendDto
import com.coltsclub.tusa.app.dto.FriendRequestDto
import com.coltsclub.tusa.app.entity.FriendEntity
import com.coltsclub.tusa.app.entity.FriendRequestEntity
import com.coltsclub.tusa.app.repository.FriendRepository
import com.coltsclub.tusa.app.repository.FriendRequestRepository
import com.coltsclub.tusa.core.AlineTwoLongsIds
import com.coltsclub.tusa.core.repository.UserRepository
import jakarta.transaction.Transactional
import java.time.LocalDateTime
import java.time.ZoneOffset
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class FriendsService(
    private val friendRepository: FriendRepository,
    private val friendRequestRepository: FriendRequestRepository,
    private val userRepository: UserRepository,
) {
    fun getFriends(userId: Long): List<FriendDto> {
        val friends = friendRepository.findByFirstUserIdOrSecondUserId(userId, userId)
        return friends.map { user ->
            var friendId = user.firstUserId
            var friendName = user.firstUserName
            var friendUniqueName = user.firstUserUniqueName
            var lastOnlineTime = user.firstUserLastOnlineTime
            if (friendId == userId) {
                friendId = user.secondUserId
                friendName = user.secondUserName
                friendUniqueName = user.secondUserUniqueName
                lastOnlineTime = user.secondUserLastOnlineTime
            }
            FriendDto(
                id = friendId,
                name = friendName,
                uniqueName = friendUniqueName,
                lastOnlineTime = lastOnlineTime.toEpochSecond(ZoneOffset.UTC),
                updateTime = user.updateTime,
                deleted = user.deleted
            )
        }
    }

    // отправить заявку в друзья
    fun addFriend(userId: Long, requestTo: Long): List<FriendRequestDto> {
        // Уже друзья то ничего не делать
        if (alreadyFriends(userId, requestTo)) {
            return emptyList()
        }

        val ids = AlineTwoLongsIds.aline(userId, requestTo)
        val firstId = ids.first
        val secondId = ids.second

        // получаем данные пользователей
        val firstUser = userRepository.findById(firstId).get()
        val secondUser = userRepository.findById(secondId).get()

        // Проверяем существует ли заявка в друзья
        // если заявка уже отправлена то ничего не делаем
        val friendRequest = friendRequestRepository.findByFirstUserIdAndSecondUserId(firstId, secondId)
        if (friendRequest != null) {
            return emptyList()
        }

        // Если нету то создаем
        val friendRequestEntity = friendRequestRepository.save(FriendRequestEntity(
            firstUserId = firstId,
            secondUserId = secondId,
            firstUserName = firstUser.name,
            secondUserName = secondUser.name,
            firstUserUniqueName = firstUser.userUniqueName,
            secondUserUniqueName = secondUser.userUniqueName,
            actorId = userId,
            deleted = false
        ))

        return listOf(
            FriendRequestDto(
                userId = secondId,
                userName = secondUser.name,
                userUniqueName = secondUser.userUniqueName,
                isRequestSender = secondId == userId,
                updateTime = friendRequestEntity.updateTime,
                deleted = friendRequestEntity.deleted
            ),
            FriendRequestDto(
                userId = firstId,
                userName = firstUser.name,
                userUniqueName = firstUser.userUniqueName,
                isRequestSender = firstId == userId,
                updateTime = friendRequestEntity.updateTime,
                deleted = friendRequestEntity.deleted
            )
        )
    }

    @Transactional
    fun deleteFriend(userId: Long, deleteFriend: Long) {
        val ids = AlineTwoLongsIds.aline(userId, deleteFriend)
        val firstId = ids.first
        val secondId = ids.second
        friendRepository.findByFirstUserIdAndSecondUserId(secondId, firstId)?.let {
            it.deleted = true
            it.updateTime = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC)
            friendRepository.save(it)
        }
    }

    fun alreadyFriends(userId: Long, withUserId: Long): Boolean {
        val ids = AlineTwoLongsIds.aline(userId, withUserId)
        val firstId = ids.first
        val secondId = ids.second
        val friendRow = friendRepository.findByFirstUserIdAndSecondUserId(firstId, secondId)
        return friendRow != null
    }

    fun deleteFriendRequest(firstId: Long, secondId: Long): FriendRequestEntity? {
        val request = friendRequestRepository.findByFirstUserIdAndSecondUserId(firstId, secondId)
        if (request != null) {
            request.deleted = true
            request.updateTime = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC)
            friendRequestRepository.save(request)
        }
        return request
    }

    @Transactional
    fun createFriends(userId: Long, requestFrom: Long) {
        val ids = AlineTwoLongsIds.aline(userId, requestFrom)
        val firstId = ids.first
        val secondId = ids.second

        // Получаем пользователей
        val firstUser = userRepository.findById(firstId).get()
        val secondUser = userRepository.findById(secondId).get()

        // удаляем заявку если есть
        deleteFriendRequest(firstId, secondId)

        // сохраняем запись о создании друзей
        friendRepository.save(FriendEntity(
            firstUserId = firstId,
            secondUserId = secondId,
            firstUserName = firstUser.name,
            secondUserName = secondUser.name,
            firstUserUniqueName = firstUser.userUniqueName,
            secondUserUniqueName = secondUser.userUniqueName,
            firstUserLastOnlineTime = firstUser.lastOnlineTime,
            secondUserLastOnlineTime = secondUser.lastOnlineTime,
        ))
    }

    fun getToMeRequests(userId: Long): List<FriendRequestDto> {
        val requests = friendRequestRepository.findByFirstUserIdOrSecondUserId(userId, userId)
        return requests.map { request ->
            var senderUserId = request.firstUserId
            var senderUserName = request.firstUserName
            var senderUniqueName = request.firstUserUniqueName
            if (request.firstUserId == userId) {
                senderUserId = request.secondUserId
                senderUserName = request.secondUserName
                senderUniqueName = request.secondUserUniqueName
            }
            FriendRequestDto(
                userId = senderUserId,
                userName = senderUserName,
                userUniqueName = senderUniqueName,
                isRequestSender = request.actorId == senderUserId,
                updateTime = request.updateTime,
                deleted = request.deleted
            )
        }
    }

    @Transactional
    fun deleteRequest(userId: Long, requestFrom: Long) {
        // Удаляем заявку в друзья
        val ids = AlineTwoLongsIds.aline(userId, requestFrom)
        val firstId = ids.first
        val secondId = ids.second
        deleteFriendRequest(firstId, secondId)
    }

    fun findUsers(userId: Long, name: String): List<FriendDto> {
        val users = userRepository.findCandidateFriends(name, userId, Pageable.ofSize(10))
        return users.map { user ->
            FriendDto( // тут вообще должен быть пользователь просто
                id = user.id!!,
                name = user.name,
                uniqueName = user.userUniqueName,
                lastOnlineTime = user.lastOnlineTime.toEpochSecond(ZoneOffset.UTC),
                updateTime = 0,
                deleted = false
            )
        }
    }

}