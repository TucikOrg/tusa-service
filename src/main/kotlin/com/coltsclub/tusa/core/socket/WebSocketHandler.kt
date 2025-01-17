package com.coltsclub.tusa.core.socket

import com.coltsclub.tusa.app.handlers.ChatBinaryHandler
import com.coltsclub.tusa.app.dto.AddLocationDto
import com.coltsclub.tusa.app.dto.AddUserDto
import com.coltsclub.tusa.app.dto.AllUsersRequest
import com.coltsclub.tusa.app.dto.AvatarAction
import com.coltsclub.tusa.app.dto.AvatarDTO
import com.coltsclub.tusa.app.dto.ChangeNameOther
import com.coltsclub.tusa.app.dto.CreatedUser
import com.coltsclub.tusa.app.dto.FakeLocation
import com.coltsclub.tusa.app.dto.FriendActionDto
import com.coltsclub.tusa.app.dto.FriendDto
import com.coltsclub.tusa.app.dto.FriendRequestActionDto
import com.coltsclub.tusa.app.dto.FriendRequestDto
import com.coltsclub.tusa.app.dto.FriendsInitializationState
import com.coltsclub.tusa.app.dto.FriendsRequestsInitializationState
import com.coltsclub.tusa.app.dto.UpdateLocationDto
import com.coltsclub.tusa.app.dto.User
import com.coltsclub.tusa.app.dto.UsersPage
import com.coltsclub.tusa.app.handlers.AdminBinaryHandler
import com.coltsclub.tusa.app.handlers.FriendsBinaryHandler
import com.coltsclub.tusa.app.handlers.full.FriendsHandlerFull
import com.coltsclub.tusa.app.repository.AvatarActionsRepository
import com.coltsclub.tusa.app.repository.FriendsActionsRepository
import com.coltsclub.tusa.app.repository.FriendsRequestsActionsRepository
import com.coltsclub.tusa.app.service.FriendsService
import com.coltsclub.tusa.core.entity.UserEntity
import com.coltsclub.tusa.app.service.AvatarService
import com.coltsclub.tusa.app.service.ChatsService
import com.coltsclub.tusa.app.service.LocationService
import com.coltsclub.tusa.app.service.MessagesService
import com.coltsclub.tusa.app.service.ProfileService
import com.coltsclub.tusa.core.repository.UserRepository
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.concurrent.ConcurrentHashMap
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import org.springframework.data.domain.PageRequest
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
    }

    override fun afterConnectionEstablished(session: WebSocketSession) {
        val user = try {session.user()} catch (e: Exception) {
            logger.error("(afterConnectionEstablished) User not found")
            return
        }
        val sessionList = sessions[user.id!!]
        if (sessionList == null) {
            sessions[user.id] = mutableListOf(session)
        } else {
            sessionList.add(session)
        }
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        val user = try {session.user()} catch (e: Exception) {
            logger.error("(afterConnectionClosed) User not found")
            return
        }

        val sessionList = sessions[user.id!!]
        sessionList?.remove(session)
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
                byteArrayOf()
            )))
            sendToSessionsOf(friendId, BinaryMessage(refreshAvatars))
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun handleBinaryMessage(session: WebSocketSession, message: BinaryMessage) {
        val user = try {session.user()} catch (e: Exception) {
            logger.error("(handleBinaryMessage) User not found")
            return
        }
        val socketMessage = Cbor.decodeFromByteArray<SocketBinaryMessage>(message.payload.array())

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
            // user actions
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
                profileService.changeName(user.id!!, name)
            }
            "change-unique-name" -> {
                val uniqueName = Cbor.decodeFromByteArray<String>(socketMessage.data)
                val success = profileService.changeUniqueName(user.id!!, uniqueName)
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

    fun sendToSessionsOf(userId: Long, binaryMessage: BinaryMessage) {
        val sessionList = sessions[userId]
        sessionList?.forEach { session ->
            if (session.isOpen) {
                session.sendMessage(binaryMessage)
            }
        }
    }

    fun WebSocketSession.user(): UserEntity {
        return (this.principal as UsernamePasswordAuthenticationToken).principal as UserEntity
    }


}