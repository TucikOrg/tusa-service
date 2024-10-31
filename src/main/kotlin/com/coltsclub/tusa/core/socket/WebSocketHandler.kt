package com.coltsclub.tusa.core.socket

import com.coltsclub.tusa.app.dto.AvatarDTO
import com.coltsclub.tusa.app.service.FriendsService
import com.coltsclub.tusa.core.entity.UserEntity
import com.coltsclub.tusa.app.service.AvatarService
import com.coltsclub.tusa.app.service.LocationService
import com.coltsclub.tusa.app.service.ProfileService
import java.util.concurrent.ConcurrentHashMap
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.web.socket.BinaryMessage
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.BinaryWebSocketHandler


class WebSocketHandler(
    private val friendsService: FriendsService,
    private val profileService: ProfileService,
    private val avatarService: AvatarService,
    private val locationService: LocationService
) : BinaryWebSocketHandler() {
    private val sessions = ConcurrentHashMap<Long, MutableList<WebSocketSession>>()

    override fun afterConnectionEstablished(session: WebSocketSession) {
        val user = session.user()
        val sessionList = sessions[user.id!!]
        if (sessionList == null) {
            sessions[user.id] = mutableListOf(session)
        } else {
            sessionList.add(session)
        }
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        val user = session.user()
        val sessionList = sessions[user.id!!]
        sessionList?.remove(session)
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun handleBinaryMessage(session: WebSocketSession, message: BinaryMessage) {
        val user = session.user()
        val socketMessage = Cbor.decodeFromByteArray<SocketBinaryMessage>(message.payload.array())
        when (socketMessage.type) {
            "friends-avatars" -> {
                // хочу получить аватарки друзей
                val friends = friendsService.getFriends(user.id!!)
                val avatars = friends.map { friend ->
                    val avatar = avatarService.getAvatarImage(friend.id!!) ?: return@map null
                    AvatarDTO(friend.id, avatar)
                }.filterNotNull()
                for (avatar in avatars) {
                    val data = Cbor.encodeToByteArray(avatar)
                    val response = Cbor.encodeToByteArray(SocketBinaryMessage("avatar", data))
                    session.sendMessage(BinaryMessage(response))
                }
            }
            "locations" -> {
                // хочу получить локации друзей
                val friends = friendsService.getFriends(user.id!!)
                val locations = locationService.getFriendsLocations(user.id, friends.map { it.id!! })
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
                val avatar = avatarService.getAvatarImage(id) ?: return
                val avatarDto = AvatarDTO(id, avatar)
                val encoded: ByteArray = Cbor.encodeToByteArray<AvatarDTO>(avatarDto)
                val response = Cbor.encodeToByteArray(SocketBinaryMessage("avatar", encoded))
                session.sendMessage(BinaryMessage(response))
            }
            "my-friends" -> {
                // получить список друзей
                val friends = friendsService.getFriends(user.id!!)
                val data = Cbor.encodeToByteArray(friends)
                val response = Cbor.encodeToByteArray(SocketBinaryMessage("my-friends", data))
                session.sendMessage(BinaryMessage(response))
            }
            "add-friend" -> {
                // отправить заявку в друзья
                val id = Cbor.decodeFromByteArray<Long>(socketMessage.data)
                val users = friendsService.addFriend(user.id!!, id)
                val dataForFriend = Cbor.encodeToByteArray(SocketBinaryMessage("add-friend", Cbor.encodeToByteArray(users.first { it.id == user.id })))
                val responseForFriend = BinaryMessage(dataForFriend)

                // отправляем возможному другу информацию о том что ему пришла заявка в друзья
                sessions[id]!!.forEach { s ->
                    s.sendMessage(responseForFriend)
                }
            }
            "accept-friend" -> {
                val id = Cbor.decodeFromByteArray<Long>(socketMessage.data)

                // новые друзья
                val friends = friendsService.acceptFriend(user.id!!, id)
                val forMeData = Cbor.encodeToByteArray(SocketBinaryMessage("accept-friend", Cbor.encodeToByteArray(friends.first { it.id == id })))
                val forMeResponse = BinaryMessage(forMeData)

                // отправляем мне информацию о том что друг добавлен
                sessions[user.id]!!.forEach { s ->
                    s.sendMessage(forMeResponse)
                }

                val forFriendData = Cbor.encodeToByteArray(SocketBinaryMessage("i-am-accepted", Cbor.encodeToByteArray(friends.first { it.id == user.id })))
                val forFriendResponse = BinaryMessage(forFriendData)

                // отправляем другу информацию о том что он добавлен
                sessions[id]!!.forEach { s ->
                    s.sendMessage(forFriendResponse)
                }
            }
            "delete-friend" -> {
                val id = Cbor.decodeFromByteArray<Long>(socketMessage.data)
                friendsService.deleteFriend(user.id!!, id)

                // отправляем мне информацию о том что друг удален
                val dataForMe = Cbor.encodeToByteArray(SocketBinaryMessage("delete-friend", Cbor.encodeToByteArray(id)))
                val responseForMe = BinaryMessage(dataForMe)
                sessions[user.id]!!.forEach { s ->
                    s.sendMessage(responseForMe)
                }

                // отправляем другу информацию о том что он удален
                val dataForFriend = Cbor.encodeToByteArray(SocketBinaryMessage("i-was-deleted-from-friends", Cbor.encodeToByteArray(user.id)))
                val responseForFriend = BinaryMessage(dataForFriend)
                sessions[id]!!.forEach { s ->
                    s.sendMessage(responseForFriend)
                }
            }
            "friends-to-me-requests" -> {
                // получить мои заявки в друзья
                val requests = friendsService.getToMeRequests(user.id!!)
                val data = Cbor.encodeToByteArray(requests)
                val response = Cbor.encodeToByteArray(SocketBinaryMessage("friends-to-me-requests", data))
                session.sendMessage(BinaryMessage(response))
            }
            "delete-request" -> {
                // удалить заявку в друзья
                val id = Cbor.decodeFromByteArray<Long>(socketMessage.data)
                friendsService.deleteRequest(user.id!!, id)

                // отправляем мне информацию о том что я удалил заявку в друзья
                val dataForMe = Cbor.encodeToByteArray(SocketBinaryMessage("delete-request", Cbor.encodeToByteArray(id)))
                val responseForMe = BinaryMessage(dataForMe)
                sessions[user.id]!!.forEach { s ->
                    s.sendMessage(responseForMe)
                }
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

    fun WebSocketSession.user(): UserEntity {
        return (this.principal as UsernamePasswordAuthenticationToken).principal as UserEntity
    }
}