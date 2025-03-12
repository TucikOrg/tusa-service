package com.coltsclub.tusa.app.service

import com.coltsclub.tusa.app.entity.FriendEntity
import com.coltsclub.tusa.app.repository.ChatRepository
import com.coltsclub.tusa.app.repository.FriendRepository
import com.coltsclub.tusa.core.entity.UserEntity
import com.coltsclub.tusa.core.enums.Role
import com.coltsclub.tusa.core.repository.UserRepository
import com.coltsclub.tusa.core.socket.SocketBinaryMessage
import com.coltsclub.tusa.core.socket.WebSocketHandler
import jakarta.transaction.Transactional
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.Optional
import kotlin.jvm.optionals.getOrNull
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.encodeToByteArray
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.web.socket.BinaryMessage

@Service
class ProfileService(
    private val userRepository: UserRepository,
    private val friendsRepository: FriendRepository,
    private val chatRepository: ChatRepository
) {
    fun getUsers(uniqueName: String, page: Pageable): Page<UserEntity> {
        return userRepository.findByUserUniqueNameContaining(uniqueName, page)
    }

    fun isUserUniqueNameAvailable(uniqueName: String): Boolean {
        return !userRepository.findByUserUniqueName(uniqueName).isPresent
    }

    @Transactional
    fun changeNameTransactional(userId: Long, name: String, friends: List<FriendEntity>) {
        val user = userRepository.findById(userId).get()
        user.name = name
        userRepository.save(user)

        // изменяем чаты
        val chats = chatRepository.findByFirstUserIdOrSecondUserIdAndDeleted(userId, userId, Pageable.unpaged(), deleted = false)
        for (chat in chats) {
            if (chat.firstUserId == userId) {
                chat.firsUserName = name
            } else {
                chat.secondUserName = name
            }
            chat.updateTime = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC)
        }
        chatRepository.saveAll(chats)

        // для друзей этого пользователя тоже нужно поменять имя
        for (friend in friends) {
            if (userId == friend.firstUserId) {
                friend.firstUserName = name
            } else {
                friend.secondUserName = name
            }
            friend.updateTime = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC)
        }
        friendsRepository.saveAll(friends)
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun changeName(userId: Long, name: String, webSocketHandler: WebSocketHandler) {
        val friends = friendsRepository.findByFirstUserIdOrSecondUserIdAndDeleted(userId, userId, deleted = false)
        changeNameTransactional(userId, name, friends)

        // отправляем изменения друзьям
        for (friend in friends) {
            val friendId = if (userId == friend.firstUserId) friend.secondUserId else friend.firstUserId
            val refreshFriends = Cbor.encodeToByteArray(SocketBinaryMessage("refresh-friends", Cbor.encodeToByteArray(
                byteArrayOf()
            )))
            webSocketHandler.sendToSessionsOf(friendId, BinaryMessage(refreshFriends))
        }

        // отправляем изменения друзьям
        for (friend in friends) {
            val friendId = if (userId == friend.firstUserId) friend.secondUserId else friend.firstUserId
            val refreshFriends = Cbor.encodeToByteArray(SocketBinaryMessage("refresh-chats", Cbor.encodeToByteArray(
                byteArrayOf()
            )))
            webSocketHandler.sendToSessionsOf(friendId, BinaryMessage(refreshFriends))
        }
    }

    fun getUser(gmail: String): Optional<UserEntity> {
        return userRepository.findByGmail(gmail)
    }

    @Transactional
    private fun changeUniqueNameTransactional(userId: Long, newUniqueName: String, friends: List<FriendEntity>) {
        val user = userRepository.findById(userId).get()
        user.userUniqueName = newUniqueName
        userRepository.save(user)

        // изменяем чаты
        val chats = chatRepository.findByFirstUserIdOrSecondUserIdAndDeleted(userId, userId, Pageable.unpaged(), deleted = false)
        for (chat in chats) {
            if (chat.firstUserId == userId) {
                chat.firstUserUniqueName = newUniqueName
            } else {
                chat.secondUserUniqueName = newUniqueName
            }
            chat.updateTime = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC)
        }
        chatRepository.saveAll(chats)

        // для друзей этого пользователя тоже нужно поменять уникальное имя
        for (friend in friends) {
            if (userId == friend.firstUserId) {
                friend.firstUserUniqueName = newUniqueName
            } else {
                friend.secondUserUniqueName = newUniqueName
            }
            friend.updateTime = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC)
        }
        friendsRepository.saveAll(friends)
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun changeUniqueName(userId: Long, newUniqueName: String, webSocketHandler: WebSocketHandler): Boolean {
        // нельзя использовать занятые имена
        val exist = userRepository.findByUserUniqueName(newUniqueName).isPresent
        if (exist) {
            return false
        }

        val friends = friendsRepository.findByFirstUserIdOrSecondUserIdAndDeleted(userId, userId, deleted = false)
        changeUniqueNameTransactional(userId, newUniqueName, friends)

        // отправляем изменения друзьям
        for (friend in friends) {
            val friendId = if (userId == friend.firstUserId) friend.secondUserId else friend.firstUserId
            val refreshFriends = Cbor.encodeToByteArray(SocketBinaryMessage("refresh-friends", Cbor.encodeToByteArray(
                byteArrayOf()
            )))
            webSocketHandler.sendToSessionsOf(friendId, BinaryMessage(refreshFriends))
        }

        // отправляем изменения друзьям
        for (friend in friends) {
            val friendId = if (userId == friend.firstUserId) friend.secondUserId else friend.firstUserId
            val refreshChats = Cbor.encodeToByteArray(SocketBinaryMessage("refresh-chats", Cbor.encodeToByteArray(
                byteArrayOf()
            )))
            webSocketHandler.sendToSessionsOf(friendId, BinaryMessage(refreshChats))
        }

        return true
    }

    fun createUser(uniqueName: String?, gmail: String, name: String): UserEntity? {
        if (uniqueName != null && !isUserUniqueNameAvailable(uniqueName)) return null

        return userRepository.save(
            UserEntity(
                userUniqueName = uniqueName,
                phone = "",
                name = name,
                role = Role.USER,
                gmail = gmail,
                firebaseToken = ""
            )
        )
    }

    fun saveFirebaseToken(id: Long, token: String) {
        val user = userRepository.findById(id).getOrNull()?: return
        user.firebaseToken = token
        userRepository.save(user)
    }

    @Transactional
    private fun updateLastOnlineTimeTransactional(userId: Long, lastOnlineTime: LocalDateTime, friends: List<FriendEntity>) {
        val user = userRepository.findById(userId).getOrNull()?: return
        user.lastOnlineTime = lastOnlineTime
        userRepository.save(user)

        // для друзей этого пользователя тоже нужно поменять
        for (friend in friends) {
            if (userId == friend.firstUserId) {
                friend.firstUserLastOnlineTime = lastOnlineTime
            } else {
                friend.secondUserLastOnlineTime = lastOnlineTime
            }
            friend.updateTime = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC)
        }
        friendsRepository.saveAll(friends)
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun updateLastOnlineTime(userId: Long, lastOnlineTile: LocalDateTime, sendToSessionsOf: (Long, BinaryMessage) -> kotlin.Int) {
        val friends = friendsRepository.findByFirstUserIdOrSecondUserIdAndDeleted(userId, userId, deleted = false)
        updateLastOnlineTimeTransactional(userId, lastOnlineTile, friends)

        // отправляем изменения друзьям
        for (friend in friends) {
            val friendId = if (userId == friend.firstUserId) friend.secondUserId else friend.firstUserId
            val refreshFriends = Cbor.encodeToByteArray(SocketBinaryMessage("refresh-friends", Cbor.encodeToByteArray(
                byteArrayOf()
            )))
            sendToSessionsOf(friendId, BinaryMessage(refreshFriends))
        }
    }
}