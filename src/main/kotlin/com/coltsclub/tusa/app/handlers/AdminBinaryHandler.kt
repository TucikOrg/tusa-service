package com.coltsclub.tusa.app.handlers

import com.coltsclub.tusa.app.dto.AddLocationDto
import com.coltsclub.tusa.app.dto.AddUserDto
import com.coltsclub.tusa.app.dto.AllUsersRequest
import com.coltsclub.tusa.app.dto.ChangeNameOther
import com.coltsclub.tusa.app.dto.CreatedUser
import com.coltsclub.tusa.app.dto.FakeLocation
import com.coltsclub.tusa.app.dto.User
import com.coltsclub.tusa.app.dto.UsersPage
import com.coltsclub.tusa.app.handlers.full.FriendsHandlerFull
import com.coltsclub.tusa.app.service.FriendsService
import com.coltsclub.tusa.app.service.LocationService
import com.coltsclub.tusa.app.service.ProfileService
import com.coltsclub.tusa.core.entity.UserEntity
import com.coltsclub.tusa.core.socket.SocketBinaryMessage
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.web.socket.BinaryMessage
import org.springframework.web.socket.WebSocketSession

@Service
class AdminBinaryHandler(
    private val profileService: ProfileService,
    private val friendsService: FriendsService,
    private val locationsService: LocationService,
    private val friendsHandlerFull: FriendsHandlerFull
) {
    lateinit var sendToSessionsOf: (Long, BinaryMessage) -> Int

    init {
        friendsHandlerFull.sendToSessionsOf = { userId, message ->
            sendToSessionsOf(userId, message)
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun handleBinaryMessage(
        socketMessage: SocketBinaryMessage,
        user: UserEntity,
        session: WebSocketSession
    ) {
        when (socketMessage.type) {
            "create-user" -> {
                val newUserData = Cbor.decodeFromByteArray<AddUserDto>(socketMessage.data)
                val created = profileService.createUser(newUserData.uniqueName, newUserData.gmail, "") ?: return
                val data = Cbor.encodeToByteArray(
                    CreatedUser(
                        name = created.name,
                        uniqueName = created.userUniqueName,
                        id = created.id!!
                    )
                )
                val response = Cbor.encodeToByteArray(SocketBinaryMessage("created-user", data))
                session.sendMessage(BinaryMessage(response))
            }
            "all-users" -> {
                val request = Cbor.decodeFromByteArray<AllUsersRequest>(socketMessage.data)
                val users = profileService.getUsers(request.uniqueName, PageRequest.of(request.page, request.size))
                val usersPage = UsersPage(
                    users = users.map { User(
                        id = it.id!!,
                        name = it.name,
                        uniqueName = it.userUniqueName,
                        gmail = it.gmail
                    ) }.toList(),
                    pagesCount = users.totalPages
                )
                val data = Cbor.encodeToByteArray(usersPage)
                val response = Cbor.encodeToByteArray(SocketBinaryMessage("all-users", data))
                session.sendMessage(BinaryMessage(response))
            }
            "force-friends" -> {
                friendsHandlerFull.createFriends(socketMessage, user)
            }
            "change-name-other" -> {
                val changeNameOther = Cbor.decodeFromByteArray<ChangeNameOther>(socketMessage.data)
                profileService.changeName(changeNameOther.userId, changeNameOther.name) { userId, message ->
                    sendToSessionsOf(userId, message)
                }
            }
            "change-unique-name-other" -> {
                val changeNameOther = Cbor.decodeFromByteArray<ChangeNameOther>(socketMessage.data)
                val success = profileService.changeUniqueName(changeNameOther.userId, changeNameOther.name, sendToSessionsOf)
            }
            "create-request-to-me" -> {
                val id = Cbor.decodeFromByteArray<Long>(socketMessage.data)
                friendsService.addFriend(id, user.id!!)
                val refreshFriendsRequests = BinaryMessage(Cbor.encodeToByteArray(SocketBinaryMessage("refresh-friends-requests", byteArrayOf())))
                sendToSessionsOf(id, refreshFriendsRequests)
                sendToSessionsOf(user.id, refreshFriendsRequests)
            }
            "fake-location" -> {
                val fakeLocation = Cbor.decodeFromByteArray<FakeLocation>(socketMessage.data)
                locationsService.fakeLocation(fakeLocation.latitude, fakeLocation.longitude, fakeLocation.userId)
                friendsHandlerFull.sendLocationsToFriends(fakeLocation.userId, AddLocationDto(fakeLocation.latitude, fakeLocation.longitude))
            }
        }
    }
}