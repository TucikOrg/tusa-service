package com.coltsclub.tusa.core.socket

import com.coltsclub.tusa.app.handlers.ChatBinaryHandler
import com.coltsclub.tusa.app.dto.AddLocationDto
import com.coltsclub.tusa.app.dto.AvatarAction
import com.coltsclub.tusa.app.dto.AvatarDTO
import com.coltsclub.tusa.app.dto.IsOnlineDto
import com.coltsclub.tusa.app.handlers.AdminBinaryHandler
import com.coltsclub.tusa.app.handlers.FriendsBinaryHandler
import com.coltsclub.tusa.app.handlers.full.FriendsHandlerFull
import com.coltsclub.tusa.app.repository.AvatarActionsRepository
import com.coltsclub.tusa.app.service.FriendsService
import com.coltsclub.tusa.core.entity.UserEntity
import com.coltsclub.tusa.app.service.AvatarService
import com.coltsclub.tusa.app.service.LocationService
import com.coltsclub.tusa.app.service.ProfileService
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.concurrent.ConcurrentHashMap
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.stereotype.Component
import org.springframework.web.socket.BinaryMessage
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.BinaryWebSocketHandler


@Component
class WebSocketHandler(
    private val friendsService: FriendsService,
    private val profileService: ProfileService,
    private val avatarService: AvatarService,
    private val locationService: LocationService,
    private val chatBinaryHandler: ChatBinaryHandler,
    private val adminBinaryHandler: AdminBinaryHandler,
    private val friendsBinaryHandler: FriendsBinaryHandler,
    private val friendsHandlerFull: FriendsHandlerFull,
    private val avatarsActionsRepository: AvatarActionsRepository
) : BinaryWebSocketHandler() {
    private val sessions = ConcurrentHashMap<Long, MutableList<WebSocketSession>>()
    private val closingSessions = ConcurrentHashMap<String, Unit>()
    private val logger = org.slf4j.LoggerFactory.getLogger(WebSocketHandler::class.java)

    init {
        chatBinaryHandler.sendToSessionsOf = { userId, message ->
            sendToSessionsOf(userId, message)
        }
        adminBinaryHandler.sendToSessionsOf = { userId, message ->
            sendToSessionsOf(userId, message)
        }
        friendsBinaryHandler.sendToSessionsOf = { userId, message ->
            sendToSessionsOf(userId, message)
        }
        friendsHandlerFull.sendToSessionsOf = { userId, message ->
            sendToSessionsOf(userId, message)
        }
        friendsHandlerFull.sessionsContains = { userId ->
            sessionsContains(userId)
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun getUserOrClose(session: WebSocketSession): UserEntity? {
        return try {session.user()} catch (e: Exception) {
            logger.error("Invalid token. Closing.")
            if (closingSessions.containsKey(session.id)) {
                // уже закрываем эту сессию
                return null
            }
            closingSessions[session.id] = Unit
            val response = Cbor.encodeToByteArray(SocketBinaryMessage("closed", Cbor.encodeToByteArray("Token invalid!")))
            session.sendMessage(BinaryMessage(response))
            return null
        }
    }

    override fun afterConnectionEstablished(session: WebSocketSession) {
        val user = getUserOrClose(session) ?: return

        removeClosedSessionsOfUser(user.id!!)
        var sessionList = sessions[user.id]
        if (sessionList == null) {
            sessionList =  mutableListOf(session)
            sessions[user.id] = sessionList
        } else {
            sessionList.add(session)
        }

        // отправляем всем друзьям информацию что пользователь онлайн
        sendToFriendsIAmOnline(user.id, online = true)

        // обновляем время последний раз в онлайне
        profileService.updateLastOnlineTime(user.id, LocalDateTime.now(ZoneOffset.UTC)) { userId, msg ->
            sendToSessionsOf(userId, msg)
        }

        val remainsUserSessions = sessionList.size
        logger.info("Connection established. User: ${user.id}. Sessions: $remainsUserSessions")
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        closingSessions.remove(session.id)
        val user = try {session.user()} catch (e: Exception) {
            logger.error("Connection closed. Code: ${status.code}")

            // Ищем сессию проходя по всем сессиям
            // Потому что найти ее быстро по пользователю не получится
            sessions.forEach { (userId, sessionList) ->
                sessionList.remove(session)
            }

            return
        }

        // Удаляем сессию из списка сессий пользователя
        val sessionList = sessions[user.id!!]
        sessionList?.remove(session)
        val remainSessions = sessionList?.size?: -1

        // отправляем всем друзьям информацию что пользователь оффлайн
        sendToFriendsIAmOnline(user.id, online = false)

        // обновляем время последний раз в онлайне
        profileService.updateLastOnlineTime(user.id, LocalDateTime.now(ZoneOffset.UTC)) { userId, msg ->
            sendToSessionsOf(userId, msg)
        }

        logger.info("Connection closed. User: ${user.id}. Remains user sessions: $remainSessions")
    }

    fun sendLocationToFriends(userId: Long, addLocationDto: AddLocationDto) {
        friendsHandlerFull.sendLocationsToFriends(userId, addLocationDto)
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun sendToFriendsAvatarUpdated(userId: Long) {
        val friends = friendsService.getFriends(userId)
        val ids = friends.map { it.id }
        ids.forEach { friendId ->
            val refreshAvatars = Cbor.encodeToByteArray(SocketBinaryMessage("refresh-avatars", Cbor.encodeToByteArray(
                LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) // отправляем серверное текущее время для сохранения временной точки синхронизации
            )))
            sendToSessionsOf(friendId, BinaryMessage(refreshAvatars))
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun handleBinaryMessage(session: WebSocketSession, message: BinaryMessage) {
        val socketMessage = Cbor.decodeFromByteArray<SocketBinaryMessage>(message.payload.array())
        val user = getUserOrClose(session) ?: return

        // Тусик мессенджер
        // обработка сообщений
        chatBinaryHandler.handleBinaryMessage(
            socketMessage,
            user,
            session
        )

        // дейсвтия для тестирования
        // админские действия
        adminBinaryHandler.handleBinaryMessage(
            socketMessage = socketMessage,
            user = user,
            session = session
        )

        // друзья и заявки в друзья
        friendsBinaryHandler.handleBinaryMessage(
            socketMessage = socketMessage,
            user = user,
            session = session
        )


        when (socketMessage.type) {
            "request-online-friends" -> {
                // запросить статус онлайн друзей
                val friends = friendsService.getFriends(user.id!!)
                val ids = friends.map { it.id }
                ids.forEach { friendId ->
                    val isFriendOnline = IsOnlineDto(userId = friendId, isOnline = sessions.containsKey(friendId))
                    val isFriendOnlineData = Cbor.encodeToByteArray(isFriendOnline)
                    val response = Cbor.encodeToByteArray(SocketBinaryMessage("is-online", isFriendOnlineData))
                    session.sendMessage(BinaryMessage(response))
                }
            }
            "firebase-token" -> {
                val token = Cbor.decodeFromByteArray<String>(socketMessage.data)
                profileService.saveFirebaseToken(user.id!!, token)
            }
            "locations" -> {
                // хочу получить локации друзей
                val friends = friendsService.getFriends(user.id!!)
                val locations = locationService.getUsersLocations(user.id, friends.map { it.id })
                val data = Cbor.encodeToByteArray(locations)
                val response = Cbor.encodeToByteArray(SocketBinaryMessage("locations", data))
                session.sendMessage(BinaryMessage(response))
            }
            "is-unique-name-available" -> {
                val uniqueName = Cbor.decodeFromByteArray<String>(socketMessage.data)
                val available = profileService.isUserUniqueNameAvailable(uniqueName)
                val response = Cbor.encodeToByteArray(SocketBinaryMessage("is-unique-name-available", Cbor.encodeToByteArray(available)))
                session.sendMessage(BinaryMessage(response))
            }
            "change-name" -> {
                val name = Cbor.decodeFromByteArray<String>(socketMessage.data)
                profileService.changeName(user.id!!, name, sendToSessionsOf = { userId, msg ->
                    sendToSessionsOf(userId, msg)
                })
            }
            "change-unique-name" -> {
                val uniqueName = Cbor.decodeFromByteArray<String>(socketMessage.data)
                val success = profileService.changeUniqueName(user.id!!, uniqueName) { userId, msg ->
                    sendToSessionsOf(userId, msg)
                }
                val response = Cbor.encodeToByteArray(SocketBinaryMessage("change-unique-name", Cbor.encodeToByteArray(success)))
                session.sendMessage(BinaryMessage(response))
            }
            "avatar" -> {
                // загрузить автарку любого пользователя
                val id = Cbor.decodeFromByteArray<Long>(socketMessage.data)
                val avatar = avatarService.getAvatarImage(id)
                val avatarDto = AvatarDTO(id, avatar)
                val encoded: ByteArray = Cbor.encodeToByteArray<AvatarDTO>(avatarDto)
                val response = Cbor.encodeToByteArray(SocketBinaryMessage("avatar", encoded))
                session.sendMessage(BinaryMessage(response))
            }
            "avatars-actions" -> {
                val timePoint = Cbor.decodeFromByteArray<Long>(socketMessage.data)
                val friends = friendsService.getFriends(user.id!!)
                val ids = friends.map { it.id }
                val actions = avatarsActionsRepository.findAllByOwnerIdInAndActionTimeGreaterThan(ids, timePoint)
                val dto = actions.map {
                    AvatarAction(
                        ownerId = it.ownerId,
                        actionType = it.actionType,
                        actionTime = it.actionTime
                    )
                }
                session.sendMessage(BinaryMessage(Cbor.encodeToByteArray(SocketBinaryMessage("avatars-actions", Cbor.encodeToByteArray(dto)))))
            }
            "find-users" -> {
                // найти пользователей по имени
                val name = Cbor.decodeFromByteArray<String>(socketMessage.data)
                val users = friendsService.findUsers(user.id!!, name)
                val data = Cbor.encodeToByteArray(users)
                val response = Cbor.encodeToByteArray(SocketBinaryMessage("find-users", data))
                session.sendMessage(BinaryMessage(response))
            }
        }
    }

    fun sendToSessionsOf(userId: Long, binaryMessage: BinaryMessage): Int {
        val sessionList = sessions[userId]
        var sentCount = 0
        sessionList?.forEach { session ->
            if (session.isOpen) {
                sentCount++
                session.sendMessage(binaryMessage)
            } else {
                sessionList.remove(session)
            }
        }
        return sentCount
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun sendToFriendsIAmOnline(userId: Long, online: Boolean) {
        val friends = friendsService.getFriends(userId)
        val ids = friends.map { it.id }
        ids.forEach { friendId ->
            val response = Cbor.encodeToByteArray(SocketBinaryMessage("is-online",
                Cbor.encodeToByteArray(IsOnlineDto(userId = userId, isOnline = online)))
            )
            sendToSessionsOf(friendId, BinaryMessage(response))
        }
    }

    fun sessionsContains(userId: Long): Boolean {
        return sessions.containsKey(userId)
    }

    fun removeClosedSessionsOfUser(userId: Long) {
        val sessionList = sessions[userId]
        sessionList?.removeIf { !it.isOpen }
    }

    fun WebSocketSession.user(): UserEntity {
        return (this.principal as UsernamePasswordAuthenticationToken).principal as UserEntity
    }
}