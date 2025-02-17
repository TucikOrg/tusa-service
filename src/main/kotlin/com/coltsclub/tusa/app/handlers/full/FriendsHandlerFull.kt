package com.coltsclub.tusa.app.handlers.full

import com.coltsclub.tusa.app.dto.AddLocationDto
import com.coltsclub.tusa.app.dto.IsOnlineDto
import com.coltsclub.tusa.app.dto.UpdateLocationDto
import com.coltsclub.tusa.app.service.FriendsService
import com.coltsclub.tusa.app.service.LocationService
import com.coltsclub.tusa.core.entity.UserEntity
import com.coltsclub.tusa.core.socket.SocketBinaryMessage
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import org.springframework.stereotype.Service
import org.springframework.web.socket.BinaryMessage

@Service
class FriendsHandlerFull(
    private val friendsService: FriendsService,
    private val locationService: LocationService
) {
    lateinit var sendToSessionsOf: (Long, BinaryMessage) -> Unit
    lateinit var sessionsContains: (Long) -> Boolean

    @OptIn(ExperimentalSerializationApi::class)
    fun createFriends(socketMessage: SocketBinaryMessage, user: UserEntity) {
        val id = Cbor.decodeFromByteArray<Long>(socketMessage.data)

        // новые друзья
        friendsService.createFriends(user.id!!, id)

        // отправляем мне информацию о том что друг добавлен
        val refreshFriends = BinaryMessage(Cbor.encodeToByteArray(SocketBinaryMessage("refresh-friends", byteArrayOf())))
        val refreshFriendsRequests = BinaryMessage(Cbor.encodeToByteArray(SocketBinaryMessage("refresh-friends-requests", byteArrayOf())))
        sendToSessionsOf(user.id, refreshFriends)
        sendToSessionsOf(user.id, refreshFriendsRequests)

        // отправляем другу информацию о том что он добавлен
        sendToSessionsOf(id, refreshFriends)
        sendToSessionsOf(id, refreshFriendsRequests)


        // отправляем мне информацию о том в онлайне ли друг
        val isFriendOnline = IsOnlineDto(userId = id, isOnline = sessionsContains(id))
        val isFriendOnlineData = Cbor.encodeToByteArray(isFriendOnline)
        val friendIsOnlineResponse = BinaryMessage(Cbor.encodeToByteArray(SocketBinaryMessage("is-online", isFriendOnlineData)))
        sendToSessionsOf(user.id, friendIsOnlineResponse)

        // отправляем другу информацию о том в онлайне ли я
        val isMeOnline = IsOnlineDto(userId = user.id, isOnline = sessionsContains(user.id))
        val isMeOnlineData = Cbor.encodeToByteArray(isMeOnline)
        val meIsOnlineResponse = BinaryMessage(Cbor.encodeToByteArray(SocketBinaryMessage("is-online", isMeOnlineData)))
        sendToSessionsOf(id, meIsOnlineResponse)


        // отправляем другу мою локацию
        val myLocation = locationService.getLastLocation(user.id)
        if (myLocation != null) {
            val response = Cbor.encodeToByteArray(SocketBinaryMessage("update-location", Cbor.encodeToByteArray(
                UpdateLocationDto(
                    whoId = user.id,
                    latitude = myLocation.latitude,
                    longitude = myLocation.longitude
                )
            )))
            sendToSessionsOf(id, BinaryMessage(response))
        }


        // отправляем мне локацию друга
        val friendLocation = locationService.getLastLocation(id)
        if (friendLocation != null) {
            val response2 = Cbor.encodeToByteArray(SocketBinaryMessage("update-location", Cbor.encodeToByteArray(
                UpdateLocationDto(
                    whoId = id,
                    latitude = friendLocation.latitude,
                    longitude = friendLocation.longitude
                )
            )))
            sendToSessionsOf(user.id, BinaryMessage(response2))
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun sendLocationsToFriends(userId: Long, addLocationDto: AddLocationDto) {
        val friends = friendsService.getFriends(userId)
        val ids = friends.map { it.id }
        ids.forEach { friendId ->
            val response = Cbor.encodeToByteArray(SocketBinaryMessage("update-location", Cbor.encodeToByteArray(
                UpdateLocationDto(
                whoId = userId,
                latitude = addLocationDto.latitude,
                longitude = addLocationDto.longitude
            )
            )))
            sendToSessionsOf(friendId, BinaryMessage(response))
        }
    }
}