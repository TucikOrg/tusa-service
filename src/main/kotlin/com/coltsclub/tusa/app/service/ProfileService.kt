package com.coltsclub.tusa.app.service

import com.coltsclub.tusa.app.entity.ChatsActionType
import com.coltsclub.tusa.app.entity.ChatsActionsEntity
import com.coltsclub.tusa.app.entity.FriendEntity
import com.coltsclub.tusa.app.entity.FriendsActionType
import com.coltsclub.tusa.app.entity.FriendsActionsEntity
import com.coltsclub.tusa.app.repository.ChatRepository
import com.coltsclub.tusa.app.repository.ChatsActionsRepository
import com.coltsclub.tusa.app.repository.FriendRepository
import com.coltsclub.tusa.app.repository.FriendsActionsRepository
import com.coltsclub.tusa.core.AlineTwoLongsIds
import com.coltsclub.tusa.core.entity.UserEntity
import com.coltsclub.tusa.core.enums.Role
import com.coltsclub.tusa.core.repository.UserRepository
import com.coltsclub.tusa.core.socket.SocketBinaryMessage
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
    private val friendsActionsRepository: FriendsActionsRepository,
    private val friendsRepository: FriendRepository,
    private val chatsActionsRepository: ChatsActionsRepository,
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
        val chats = chatRepository.findByFirstUserIdOrSecondUserId(userId, userId, Pageable.unpaged())
        for (chat in chats) {
            if (chat.firstUserId == userId) {
                chat.firsUserName = name
            } else {
                chat.secondUserName = name
            }
        }
        chatRepository.saveAll(chats)

        // для друзей этого пользователя тоже нужно поменять имя

        for (friend in friends) {
            if (userId == friend.firstUserId) {
                friend.firstUserName = name
            } else {
                friend.secondUserName = name
            }
        }
        friendsRepository.saveAll(friends)

        val saveActions = mutableListOf<FriendsActionsEntity>()
        for (friend in friends) {
            val firstId = friend.firstUserId
            val secondId = friend.secondUserId

            var useFirstUserName = friend.firstUserName
            var useSecondUserName = friend.secondUserName
            if (userId == firstId) {
                useFirstUserName = name
            } else {
                useSecondUserName = name
            }

            val changeEntity = FriendsActionsEntity(
                firstUserId = firstId,
                secondUserId = secondId,
                firstUserName = useFirstUserName,
                secondUserName = useSecondUserName,
                firstUserUniqueName = friend.firstUserUniqueName,
                secondUserUniqueName = friend.secondUserUniqueName,
                actionType = FriendsActionType.CHANGE,
                actionTime = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC),
                firstUserLastOnlineTime = friend.firstUserLastOnlineTime,
                secondUserLastOnlineTime = friend.secondUserLastOnlineTime
            )
            saveActions.add(changeEntity)
        }

        // сохраняем изменения (имени) для друзей
        friendsActionsRepository.saveAll(saveActions)

        // теперь нужно имена в чатах обновить
        val chatChangeActions = mutableListOf<ChatsActionsEntity>()
        for (friend in friends) {
            val firstId = friend.firstUserId
            val secondId = friend.secondUserId

            // есть ли чат между этими пользователями
            val chat = chats.firstOrNull { it.firstUserId == firstId && it.secondUserId == secondId } ?: continue

            var useFirstUserName = chat.firsUserName
            var useSecondUserName = chat.secondUserName
            if (userId == firstId) {
                useFirstUserName = name
            } else {
                useSecondUserName = name
            }

            val changeChatAction = ChatsActionsEntity(
                chatId = chat.id!!,
                firstUserId = firstId,
                secondUserId = secondId,
                firsUserName = useFirstUserName,
                secondUserName = useSecondUserName,
                firstUserUniqueName = chat.firstUserUniqueName,
                secondUserUniqueName = chat.secondUserUniqueName,
                actionType = ChatsActionType.CHANGE,
                actionTime = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC)
            )
            chatChangeActions.add(changeChatAction)
        }

        // сохраняем изменения (имени) для чатов
        chatsActionsRepository.saveAll(chatChangeActions)
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun changeName(userId: Long, name: String, sendToSessionsOf: (Long, BinaryMessage) -> Int) {
        val friends = friendsRepository.findByFirstUserIdOrSecondUserId(userId, userId)
        changeNameTransactional(userId, name, friends)

        // отправляем изменения друзьям
        for (friend in friends) {
            val friendId = if (userId == friend.firstUserId) friend.secondUserId else friend.firstUserId
            val refreshFriends = Cbor.encodeToByteArray(SocketBinaryMessage("refresh-friends", Cbor.encodeToByteArray(
                byteArrayOf()
            )))
            sendToSessionsOf(friendId, BinaryMessage(refreshFriends))
        }

        // отправляем изменения друзьям
        for (friend in friends) {
            val friendId = if (userId == friend.firstUserId) friend.secondUserId else friend.firstUserId
            val refreshFriends = Cbor.encodeToByteArray(SocketBinaryMessage("refresh-chats", Cbor.encodeToByteArray(
                byteArrayOf()
            )))
            sendToSessionsOf(friendId, BinaryMessage(refreshFriends))
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
        val chats = chatRepository.findByFirstUserIdOrSecondUserId(userId, userId, Pageable.unpaged())
        for (chat in chats) {
            if (chat.firstUserId == userId) {
                chat.firstUserUniqueName = newUniqueName
            } else {
                chat.secondUserUniqueName = newUniqueName
            }
        }
        chatRepository.saveAll(chats)

        // для друзей этого пользователя тоже нужно поменять уникальное имя
        for (friend in friends) {
            if (userId == friend.firstUserId) {
                friend.firstUserUniqueName = newUniqueName
            } else {
                friend.secondUserUniqueName = newUniqueName
            }
        }
        friendsRepository.saveAll(friends)


        val saveActions = mutableListOf<FriendsActionsEntity>()
        for (friend in friends) {
            val firstId = friend.firstUserId
            val secondId = friend.secondUserId

            var useFirstUserUniqueName = friend.firstUserName
            var useSecondUserUniqueName = friend.secondUserName
            if (userId == firstId) {
                useFirstUserUniqueName = newUniqueName
            } else {
                useSecondUserUniqueName = newUniqueName
            }

            val changeEntity = FriendsActionsEntity(
                firstUserId = firstId,
                secondUserId = secondId,
                firstUserName = friend.firstUserName,
                secondUserName = friend.secondUserName,
                firstUserUniqueName = useFirstUserUniqueName,
                secondUserUniqueName = useSecondUserUniqueName,
                actionType = FriendsActionType.CHANGE,
                actionTime = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC),
                firstUserLastOnlineTime = friend.firstUserLastOnlineTime,
                secondUserLastOnlineTime = friend.secondUserLastOnlineTime
            )
            saveActions.add(changeEntity)
        }

        // сохраняем изменения (имени) для друзей
        friendsActionsRepository.saveAll(saveActions)

        // теперь нужно имена в чатах обновить
        val chatChangeActions = mutableListOf<ChatsActionsEntity>()
        for (friend in friends) {
            val firstId = friend.firstUserId
            val secondId = friend.secondUserId

            // есть ли чат между этими пользователями
            val chat = chats.firstOrNull { it.firstUserId == firstId && it.secondUserId == secondId } ?: continue

            var useFirstUserUniqueName = chat.firstUserUniqueName
            var useSecondUserUniqueName = chat.secondUserUniqueName
            if (userId == firstId) {
                useFirstUserUniqueName = newUniqueName
            } else {
                useSecondUserUniqueName = newUniqueName
            }

            val changeChatAction = ChatsActionsEntity(
                chatId = chat.id!!,
                firstUserId = firstId,
                secondUserId = secondId,
                firsUserName = chat.firsUserName,
                secondUserName = chat.secondUserName,
                firstUserUniqueName = useFirstUserUniqueName,
                secondUserUniqueName = useSecondUserUniqueName,
                actionType = ChatsActionType.CHANGE,
                actionTime = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC)
            )
            chatChangeActions.add(changeChatAction)
        }

        // сохраняем изменения (уникальных имен) для чатов
        chatsActionsRepository.saveAll(chatChangeActions)
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun changeUniqueName(userId: Long, newUniqueName: String, sendToSessionsOf: (Long, BinaryMessage) -> Int): Boolean {
        // нельзя использовать занятые имена
        val exist = userRepository.findByUserUniqueName(newUniqueName).isPresent
        if (exist) {
            return false
        }

        val friends = friendsRepository.findByFirstUserIdOrSecondUserId(userId, userId)
        changeUniqueNameTransactional(userId, newUniqueName, friends)

        // отправляем изменения друзьям
        for (friend in friends) {
            val friendId = if (userId == friend.firstUserId) friend.secondUserId else friend.firstUserId
            val refreshFriends = Cbor.encodeToByteArray(SocketBinaryMessage("refresh-friends", Cbor.encodeToByteArray(
                byteArrayOf()
            )))
            sendToSessionsOf(friendId, BinaryMessage(refreshFriends))
        }

        // отправляем изменения друзьям
        for (friend in friends) {
            val friendId = if (userId == friend.firstUserId) friend.secondUserId else friend.firstUserId
            val refreshChats = Cbor.encodeToByteArray(SocketBinaryMessage("refresh-chats", Cbor.encodeToByteArray(
                byteArrayOf()
            )))
            sendToSessionsOf(friendId, BinaryMessage(refreshChats))
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
        }
        friendsRepository.saveAll(friends)


        val saveActions = mutableListOf<FriendsActionsEntity>()
        for (friend in friends) {
            val firstId = friend.firstUserId
            val secondId = friend.secondUserId

            var useFirst = friend.firstUserLastOnlineTime
            var useSecond = friend.secondUserLastOnlineTime
            if (userId == firstId) {
                useFirst = lastOnlineTime
            } else {
                useSecond = lastOnlineTime
            }

            val changeEntity = FriendsActionsEntity(
                firstUserId = firstId,
                secondUserId = secondId,
                firstUserName = friend.firstUserName,
                secondUserName = friend.secondUserName,
                firstUserUniqueName = friend.firstUserUniqueName,
                secondUserUniqueName = friend.secondUserUniqueName,
                actionType = FriendsActionType.CHANGE,
                actionTime = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC),
                firstUserLastOnlineTime = useFirst,
                secondUserLastOnlineTime = useSecond
            )
            saveActions.add(changeEntity)
        }

        // сохраняем изменения для друзей
        friendsActionsRepository.saveAll(saveActions)
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun updateLastOnlineTime(userId: Long, lastOnlineTile: LocalDateTime, sendToSessionsOf: (Long, BinaryMessage) -> kotlin.Int) {
        val friends = friendsRepository.findByFirstUserIdOrSecondUserId(userId, userId)
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