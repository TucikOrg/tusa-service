package com.coltsclub.tusa.app.handlers

import com.coltsclub.tusa.app.dto.FriendDto
import com.coltsclub.tusa.app.dto.FriendRequestDto
import com.coltsclub.tusa.app.dto.FriendsInitializationState
import com.coltsclub.tusa.app.dto.FriendsRequestsInitializationState
import com.coltsclub.tusa.app.handlers.full.FriendsHandlerFull
import com.coltsclub.tusa.app.repository.FriendRepository
import com.coltsclub.tusa.app.repository.FriendRequestRepository
import com.coltsclub.tusa.app.service.FriendsService
import com.coltsclub.tusa.core.entity.UserEntity
import com.coltsclub.tusa.core.socket.SocketBinaryMessage
import com.coltsclub.tusa.core.socket.WebSocketHandler
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import org.springframework.stereotype.Service
import org.springframework.web.socket.BinaryMessage
import org.springframework.web.socket.WebSocketSession

@Service
class FriendsBinaryHandler(
    private val friendsService: FriendsService,
    private val friendRepository: FriendRepository,
    private val friendRequestRepository: FriendRequestRepository,
    private val friendsHandlerFull: FriendsHandlerFull
) {
    @OptIn(ExperimentalSerializationApi::class)
    fun handleBinaryMessage(
        socketMessage: SocketBinaryMessage,
        user: UserEntity,
        session: WebSocketSession,
        webSocketHandler: WebSocketHandler
    ) {
        when (socketMessage.type) {
            "my-friends" -> {
                // получить список друзей
                val friends = friendsService.getFriends(user.id!!)
                val initState = FriendsInitializationState(
                    friends = friends,
                    timePoint = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC)
                )
                val data = Cbor.encodeToByteArray(initState)
                val response = Cbor.encodeToByteArray(SocketBinaryMessage("my-friends", data))
                webSocketHandler.sendMessageValidation(session, BinaryMessage(response))
            }
            "add-friend" -> {
                // Отправить заявку в друзья
                val id = Cbor.decodeFromByteArray<Long>(socketMessage.data)
                friendsService.addFriend(user.id!!, id)

                // Отправляем возможному другу информацию о том что ему пришла заявка в друзья
                val refreshFriendsRequests = BinaryMessage(Cbor.encodeToByteArray(SocketBinaryMessage("refresh-friends-requests", byteArrayOf())))
                webSocketHandler.sendToSessionsOf(id, refreshFriendsRequests)
                webSocketHandler.sendToSessionsOf(user.id, refreshFriendsRequests)
            }
            "friends-requests-actions" -> {
                val appTimePoint = Cbor.decodeFromByteArray<Long>(socketMessage.data)
                val actions = friendRequestRepository.findAllByFirstUserIdOrSecondUserIdAndUpdateTimeGreaterThan(
                    firstUserId = user.id!!,
                    secondUserId = user.id,
                    updateTime = appTimePoint
                )
                val friendsRequestsDto = actions.map { act ->
                    var userId = act.firstUserId
                    var userName = act.firstUserName
                    var userUniqueName = act.firstUserUniqueName
                    if (act.firstUserId == user.id) {
                        userId = act.secondUserId
                        userName = act.secondUserName
                        userUniqueName = act.secondUserUniqueName
                    }

                    FriendRequestDto(
                        userId = userId,
                        userName = userName,
                        userUniqueName = userUniqueName,
                        isRequestSender = act.actorId == userId,
                        updateTime = act.updateTime,
                        deleted = act.deleted
                    )
                }
                val actionsBinary = Cbor.encodeToByteArray(SocketBinaryMessage("friends-requests-actions", Cbor.encodeToByteArray(friendsRequestsDto)))
                val response = BinaryMessage(actionsBinary)
                webSocketHandler.sendMessageValidation(session, response)
            }
            "friends-actions" -> {
                val appTimePoint = Cbor.decodeFromByteArray<Long>(socketMessage.data)
                val friends = friendRepository.findAllByFirstUserIdOrSecondUserIdAndUpdateTimeGreaterThan(
                    firstUserId = user.id!!,
                    secondUserId = user.id,
                    updateTime = appTimePoint
                )
                val friendsActionsDto = friends.map { act ->
                    var userId = act.firstUserId
                    var userName = act.firstUserName
                    var userUniqueName = act.firstUserUniqueName
                    var lastOnlineTime = act.firstUserLastOnlineTime
                    if (act.firstUserId == user.id) {
                        userId = act.secondUserId
                        userName = act.secondUserName
                        userUniqueName = act.secondUserUniqueName
                        lastOnlineTime = act.secondUserLastOnlineTime
                    }
                    FriendDto(
                        id = userId,
                        name = userName,
                        uniqueName = userUniqueName,
                        lastOnlineTime = lastOnlineTime.toEpochSecond(ZoneOffset.UTC),
                        updateTime = act.updateTime,
                        deleted = act.deleted
                    )
                }

                val actionsBinary = Cbor.encodeToByteArray(SocketBinaryMessage("friends-actions", Cbor.encodeToByteArray(friendsActionsDto)))
                val response = BinaryMessage(actionsBinary)
                webSocketHandler.sendMessageValidation(session, response)
            }
            "accept-friend" -> {
                // один участник которому отправили заявку в друзья принимает ее и добавляет другого участника в друзья
                friendsHandlerFull.createFriends(socketMessage, user, webSocketHandler)
            }
            "delete-friend" -> {
                val id = Cbor.decodeFromByteArray<Long>(socketMessage.data)
                friendsService.deleteFriend(user.id!!, id)

                // отправляем мне информацию о том что друг удален
                val dataForMe = Cbor.encodeToByteArray(SocketBinaryMessage("refresh-friends", byteArrayOf()))
                webSocketHandler.sendToSessionsOf(user.id, BinaryMessage(dataForMe))

                // отправляем другу информацию о том что он удален
                val dataForFriend = Cbor.encodeToByteArray(SocketBinaryMessage("refresh-friends", byteArrayOf()))
                webSocketHandler.sendToSessionsOf(id, BinaryMessage(dataForFriend))
            }
            "friends-requests" -> {
                // получить мои заявки в друзья
                val requests = friendsService.getToMeRequests(user.id!!)
                val friendsRequestsInitializationState = FriendsRequestsInitializationState(
                    friends = requests,
                    timePoint = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC)
                )
                val data = Cbor.encodeToByteArray(friendsRequestsInitializationState)
                val response = Cbor.encodeToByteArray(SocketBinaryMessage("friends-requests", data))
                webSocketHandler.sendToSessionsOf(user.id, BinaryMessage(response))
            }
            "delete-request" -> {
                // удалить заявку в друзья которую мне отправили
                val id = Cbor.decodeFromByteArray<Long>(socketMessage.data)
                friendsService.deleteRequest(user.id!!, id)

                // обновить мое состояние локальное
                val dataForMe = Cbor.encodeToByteArray(SocketBinaryMessage("refresh-friends-requests", Cbor.encodeToByteArray(id)))
                webSocketHandler.sendToSessionsOf(user.id, BinaryMessage(dataForMe))
            }
        }
    }
}