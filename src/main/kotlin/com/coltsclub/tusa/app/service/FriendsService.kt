package com.coltsclub.tusa.app.service

import com.coltsclub.tusa.app.dto.FriendDto
import com.coltsclub.tusa.app.dto.FriendRequestDto
import com.coltsclub.tusa.app.entity.FriendEntity
import com.coltsclub.tusa.app.entity.FriendRequestEntity
import com.coltsclub.tusa.app.entity.FriendsActionType
import com.coltsclub.tusa.app.entity.FriendsActionsEntity
import com.coltsclub.tusa.app.entity.FriendsRequestsActionsEntity
import com.coltsclub.tusa.app.repository.FriendRepository
import com.coltsclub.tusa.app.repository.FriendRequestRepository
import com.coltsclub.tusa.app.repository.FriendsActionsRepository
import com.coltsclub.tusa.app.repository.FriendsRequestsActionsRepository
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
    private val friendsActionsRepository: FriendsActionsRepository,
    private val friendsRequestsActionsRepository: FriendsRequestsActionsRepository
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
                lastOnlineTime = lastOnlineTime.toEpochSecond(ZoneOffset.UTC)
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
        friendRequestRepository.save(FriendRequestEntity(
            firstUserId = firstId,
            secondUserId = secondId,
            firstUserName = firstUser.name,
            secondUserName = secondUser.name,
            firstUserUniqueName = firstUser.userUniqueName,
            secondUserUniqueName = secondUser.userUniqueName,
            actorId = userId
        ))

        // создаем действие создания заявки в друзья
        friendsRequestsActionsRepository.save(FriendsRequestsActionsEntity(
            firstUserId = firstId,
            secondUserId = secondId,
            firstUserName = firstUser.name,
            secondUserName = secondUser.name,
            firstUserUniqueName = firstUser.userUniqueName,
            secondUserUniqueName = secondUser.userUniqueName,
            actorId = userId,
            actionTime = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC),
            actionType = FriendsActionType.ADD
        ))

        return listOf(
            FriendRequestDto(
                userId = secondId,
                userName = secondUser.name,
                userUniqueName = secondUser.userUniqueName,
                isRequestSender = secondId == userId
            ),
            FriendRequestDto(
                userId = firstId,
                userName = firstUser.name,
                userUniqueName = firstUser.userUniqueName,
                isRequestSender = firstId == userId
            )
        )
    }

    @Transactional
    fun deleteFriend(userId: Long, deleteFriend: Long) {
        val ids = AlineTwoLongsIds.aline(userId, deleteFriend)
        val firstId = ids.first
        val secondId = ids.second
        friendRepository.deleteByFirstUserIdAndSecondUserId(firstId, secondId)

        val firstUser = userRepository.findById(firstId).get()
        val secondUser = userRepository.findById(secondId).get()

        // cохраняем действия удаления
        friendsActionsRepository.save(
            FriendsActionsEntity(
                firstUserId = firstId,
                secondUserId = secondId,
                firstUserName = firstUser.name,
                secondUserName = secondUser.name,
                firstUserUniqueName = firstUser.userUniqueName,
                secondUserUniqueName = secondUser.userUniqueName,
                actionType = FriendsActionType.DELETE,
                actionTime = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC),
                firstUserLastOnlineTime = firstUser.lastOnlineTime,
                secondUserLastOnlineTime = secondUser.lastOnlineTime
            )
        )
    }

    fun alreadyFriends(userId: Long, withUserId: Long): Boolean {
        val ids = AlineTwoLongsIds.aline(userId, withUserId)
        val firstId = ids.first
        val secondId = ids.second
        val friendRow = friendRepository.findByFirstUserIdAndSecondUserId(firstId, secondId)
        return friendRow != null
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
        val requestDeleted = friendRequestRepository.deleteByFirstUserIdAndSecondUserId(firstId, secondId) > 0

        if (requestDeleted) {
            // сохраняем действие удаления заявки
            friendsRequestsActionsRepository.save(FriendsRequestsActionsEntity(
                firstUserId = firstId,
                secondUserId = secondId,
                firstUserName = firstUser.name,
                secondUserName = secondUser.name,
                firstUserUniqueName = firstUser.userUniqueName,
                secondUserUniqueName = secondUser.userUniqueName,
                actorId = userId,
                actionType = FriendsActionType.DELETE,
                actionTime = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC)
            ))
        }

        // сохраняем запись о создании друзей
        friendRepository.save(FriendEntity(
            firstUserId = firstId,
            secondUserId = secondId,
            firstUserName = firstUser.name,
            secondUserName = secondUser.name,
            firstUserUniqueName = firstUser.userUniqueName,
            secondUserUniqueName = secondUser.userUniqueName,
            firstUserLastOnlineTime = firstUser.lastOnlineTime,
            secondUserLastOnlineTime = secondUser.lastOnlineTime
        ))

        // cохраняем действия добавления в друзья
        friendsActionsRepository.save(FriendsActionsEntity(
            firstUserId = firstId,
            secondUserId = secondId,
            firstUserName = firstUser.name,
            secondUserName = secondUser.name,
            firstUserUniqueName = firstUser.userUniqueName,
            secondUserUniqueName = secondUser.userUniqueName,
            actionTime = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC),
            actionType = FriendsActionType.ADD,
            firstUserLastOnlineTime = firstUser.lastOnlineTime,
            secondUserLastOnlineTime = secondUser.lastOnlineTime
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
                isRequestSender = request.actorId == senderUserId
            )
        }
    }

    @Transactional
    fun deleteRequest(userId: Long, requestFrom: Long) {
        // Удаляем заявку в друзья
        val ids = AlineTwoLongsIds.aline(userId, requestFrom)
        val firstId = ids.first
        val secondId = ids.second
        friendRequestRepository.deleteByFirstUserIdAndSecondUserId(firstId, secondId)

        val firstUser = userRepository.findById(firstId).get()
        val secondUser = userRepository.findById(secondId).get()

        // сохраняем действие удаления заявки
        friendsRequestsActionsRepository.save(FriendsRequestsActionsEntity(
            firstUserId = firstId,
            firstUserName = firstUser.name,
            firstUserUniqueName = firstUser.userUniqueName,
            secondUserId = secondId,
            secondUserName = secondUser.name,
            secondUserUniqueName = secondUser.userUniqueName,
            actorId = userId,
            actionTime = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC),
            actionType = FriendsActionType.DELETE
        ))
    }

    fun findUsers(userId: Long, name: String): List<FriendDto> {
        val users = userRepository.findCandidateFriends(name, userId, Pageable.ofSize(10))
        return users.map { user ->
            FriendDto(
                id = user.id!!,
                name = user.name,
                uniqueName = user.userUniqueName,
                lastOnlineTime = user.lastOnlineTime.toEpochSecond(ZoneOffset.UTC)
            )
        }
    }

}