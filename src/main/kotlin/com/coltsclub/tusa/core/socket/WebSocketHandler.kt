package com.coltsclub.tusa.core.socket

import com.coltsclub.tusa.app.dto.AddUserDto
import com.coltsclub.tusa.app.dto.AllUsersRequest
import com.coltsclub.tusa.app.dto.AvatarDTO
import com.coltsclub.tusa.app.dto.ChangeNameOther
import com.coltsclub.tusa.app.dto.ChatResponse
import com.coltsclub.tusa.app.dto.ChatsResponse
import com.coltsclub.tusa.app.dto.CreatedUser
import com.coltsclub.tusa.app.dto.FakeLocation
import com.coltsclub.tusa.app.dto.MessageResponse
import com.coltsclub.tusa.app.dto.MuteSetResult
import com.coltsclub.tusa.app.dto.NewChat
import com.coltsclub.tusa.app.dto.RequestChats
import com.coltsclub.tusa.app.dto.RequestLastMessages
import com.coltsclub.tusa.app.dto.RequestMessages
import com.coltsclub.tusa.app.dto.ResponseMessages
import com.coltsclub.tusa.app.dto.SendMessage
import com.coltsclub.tusa.app.dto.SetMuteState
import com.coltsclub.tusa.app.dto.User
import com.coltsclub.tusa.app.dto.UsersPage
import com.coltsclub.tusa.app.service.FriendsService
import com.coltsclub.tusa.core.entity.UserEntity
import com.coltsclub.tusa.app.service.AvatarService
import com.coltsclub.tusa.app.service.ChatsService
import com.coltsclub.tusa.app.service.LocationService
import com.coltsclub.tusa.app.service.MessagesService
import com.coltsclub.tusa.app.service.ProfileService
import java.time.ZoneOffset
import java.util.concurrent.ConcurrentHashMap
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.web.socket.BinaryMessage
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.BinaryWebSocketHandler


class WebSocketHandler(
    private val friendsService: FriendsService,
    private val profileService: ProfileService,
    private val avatarService: AvatarService,
    private val locationService: LocationService,
    private val chatService: ChatsService,
    private val messagesService: MessagesService
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
            "messages" -> {
                val requestMessages = Cbor.decodeFromByteArray<RequestMessages>(socketMessage.data)
                val messages = messagesService.getMessages(requestMessages.chatId,
                    PageRequest.of(requestMessages.page, requestMessages.size, Sort.by(Sort.Direction.DESC, "creation"))
                )
                val responseMessages = messages.map {
                    MessageResponse(
                        message = it.message,
                        ownerId = it.ownerId,
                        toId = it.toId,
                        chatId = it.chatId,
                        payload = it.payload,
                        creation = it.creation.toInstant(ZoneOffset.UTC).toEpochMilli(),
                        deletedOwner = it.deletedOwner,
                        deletedTo = it.deletedTo,
                        changed = it.changed,
                        read = it.read
                    )
                }
                val data = Cbor.encodeToByteArray(ResponseMessages(
                    messages = responseMessages.toList(),
                    totalPages = messages.totalPages,
                    chatId = requestMessages.chatId,
                    page = requestMessages.page
                ))
                val response = Cbor.encodeToByteArray(SocketBinaryMessage("messages", data))
                session.sendMessage(BinaryMessage(response))
            }
            "chats" -> {
                val requestChats = Cbor.decodeFromByteArray<RequestChats>(socketMessage.data)
                val chats = chatService.getChats(user.id!!, PageRequest.of(requestChats.page, requestChats.size))
                val chatsResp = chats.map { chat ->
                    ChatResponse(
                        chatId = chat.chatId,
                        ownerId = chat.ownerId,
                        toId = chat.toId,
                        lastMessage = chat.lastMessage,
                        lastMessageOwner = chat.lastMessageOwner,
                        muted = chat.muted,
                    )
                }
                val dataR = ChatsResponse(
                    chats = chatsResp.toList(),
                    totalPages = chats.totalPages,
                    page = requestChats.page
                )
                val data = Cbor.encodeToByteArray(dataR)
                val response = Cbor.encodeToByteArray(SocketBinaryMessage("chats", data))
                session.sendMessage(BinaryMessage(response))
            }
            "find-chat" -> {
                val toId = Cbor.decodeFromByteArray<Long>(socketMessage.data)
                val chat = chatService.findChat(user.id!!, toId)
                if (chat != null) {
                    val data = Cbor.encodeToByteArray(ChatResponse(
                        chatId = chat.chatId,
                        ownerId = chat.ownerId,
                        toId = chat.toId,
                        lastMessage = chat.lastMessage,
                        lastMessageOwner = chat.lastMessageOwner,
                        muted = chat.muted
                    ))
                    val response = Cbor.encodeToByteArray(SocketBinaryMessage("chat", data))
                    session.sendMessage(BinaryMessage(response))
                } else {
                    val data = Cbor.encodeToByteArray(SocketBinaryMessage("no-chat", Cbor.encodeToByteArray(toId)))
                    session.sendMessage(BinaryMessage(data))
                }

            }
            "send-message" -> {
                val sendMessage = Cbor.decodeFromByteArray<SendMessage>(socketMessage.data)
                val result = chatService.sendMessage(sendMessage, user.id!!)
                if (result.chatsNew) {
                    for (chat in result.newChats) {
                        val data = Cbor.encodeToByteArray(NewChat(
                            chatId = chat.chatId,
                            ownerId = chat.ownerId,
                            toId = chat.toId,
                            lastMessage = chat.lastMessage,
                            lastMessageOwner = chat.lastMessageOwner
                        ))
                        val response = Cbor.encodeToByteArray(SocketBinaryMessage("new-chat", data))
                        sessions[chat.ownerId]?.forEach {
                            it.sendMessage(BinaryMessage(response))
                        }
                    }
                }

                val notifyMessage = Cbor.encodeToByteArray(MessageResponse(
                    message = result.messageEntity.message,
                    ownerId = result.messageEntity.ownerId,
                    toId = result.messageEntity.toId,
                    chatId = result.messageEntity.chatId,
                    payload = result.messageEntity.payload,
                    creation = result.messageEntity.creation.toInstant(ZoneOffset.UTC).toEpochMilli(),
                    deletedOwner = result.messageEntity.deletedOwner,
                    deletedTo = result.messageEntity.deletedTo,
                    changed = result.messageEntity.changed,
                    read = result.messageEntity.read
                ))
                val data = Cbor.encodeToByteArray(SocketBinaryMessage("message", notifyMessage))
                sessions[result.messageEntity.toId]?.forEach { s ->
                    s.sendMessage(BinaryMessage(data))
                }
            }
            "set-mute-state" -> {
                val setMuteState = Cbor.decodeFromByteArray<SetMuteState>(socketMessage.data)
                val chat = chatService.setMuteStateChat(setMuteState, user.id!!)
                if (chat != null) {
                    val muteSetResult = MuteSetResult(
                        chatId = chat.chatId,
                        state = chat.muted
                    )
                    val data = Cbor.encodeToByteArray(SocketBinaryMessage("set-mute-state", Cbor.encodeToByteArray(muteSetResult)))
                    sessions[chat.ownerId]?.forEach {
                        it.sendMessage(BinaryMessage(data))
                    }
                }
            }
            "delete-chat" -> {
                val chatId = Cbor.decodeFromByteArray<Long>(socketMessage.data)
                val chat = chatService.deleteChat(chatId, user.id!!)
                if (chat != null) {
                    val data = Cbor.encodeToByteArray(SocketBinaryMessage("delete-chat", Cbor.encodeToByteArray(chat.chatId)))
                    sessions[chat.ownerId]?.forEach {
                        it.sendMessage(BinaryMessage(data))
                    }
                }
            }

            // Admin actions
            "create-user" -> {
                val newUserData = Cbor.decodeFromByteArray<AddUserDto>(socketMessage.data)
                val created = profileService.createUser(newUserData.uniqueName, newUserData.gmail, null) ?: return
                val data = Cbor.encodeToByteArray(CreatedUser(
                    name = created.name,
                    uniqueName = created.userUniqueName,
                    id = created.id!!
                ))
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
                val withUserId = Cbor.decodeFromByteArray<Long>(socketMessage.data)

                val friends = friendsService.createFriends(user.id!!, withUserId)
                if (friends.isEmpty())return

                val forMeData = Cbor.encodeToByteArray(SocketBinaryMessage("accept-friend", Cbor.encodeToByteArray(friends.first { it.id == withUserId })))
                val forMeResponse = BinaryMessage(forMeData)

                // отправляем мне информацию о том что друг добавлен
                sessions[user.id]?.forEach { s ->
                    s.sendMessage(forMeResponse)
                }
                val forFriendData = Cbor.encodeToByteArray(SocketBinaryMessage("i-am-accepted", Cbor.encodeToByteArray(friends.first { it.id == user.id })))
                val forFriendResponse = BinaryMessage(forFriendData)

                // отправляем другу информацию о том что он добавлен
                sessions[withUserId]?.forEach { s ->
                    s.sendMessage(forFriendResponse)
                }
            }
            "change-name-other" -> {
                val changeNameOther = Cbor.decodeFromByteArray<ChangeNameOther>(socketMessage.data)
                profileService.changeName(changeNameOther.userId, changeNameOther.name)
            }
            "change-unique-name-other" -> {
                val changeNameOther = Cbor.decodeFromByteArray<ChangeNameOther>(socketMessage.data)
                val success = profileService.changeUniqueName(changeNameOther.userId, changeNameOther.name)
            }
            "create-request-to-me" -> {
                val id = Cbor.decodeFromByteArray<Long>(socketMessage.data)
                val users = friendsService.addFriend(id, user.id!!)
                val dataForMe = BinaryMessage(Cbor.encodeToByteArray(SocketBinaryMessage("add-friend", Cbor.encodeToByteArray(users.first { it.id == id }))))

                sessions[user.id]?.forEach { s ->
                    s.sendMessage(dataForMe)
                }
            }
            "fake-location" -> {
                val fakeLocation = Cbor.decodeFromByteArray<FakeLocation>(socketMessage.data)
                locationService.fakeLocation(fakeLocation.latitude, fakeLocation.longitude, fakeLocation.userId)
            }


            // user actions
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
                sessions[id]?.forEach { s ->
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
                sessions[user.id]?.forEach { s ->
                    s.sendMessage(forMeResponse)
                }

                val forFriendData = Cbor.encodeToByteArray(SocketBinaryMessage("i-am-accepted", Cbor.encodeToByteArray(friends.first { it.id == user.id })))
                val forFriendResponse = BinaryMessage(forFriendData)

                // отправляем другу информацию о том что он добавлен
                sessions[id]?.forEach { s ->
                    s.sendMessage(forFriendResponse)
                }
            }
            "delete-friend" -> {
                val id = Cbor.decodeFromByteArray<Long>(socketMessage.data)
                friendsService.deleteFriend(user.id!!, id)

                // отправляем мне информацию о том что друг удален
                val dataForMe = Cbor.encodeToByteArray(SocketBinaryMessage("delete-friend", Cbor.encodeToByteArray(id)))
                val responseForMe = BinaryMessage(dataForMe)
                sessions[user.id]?.forEach { s ->
                    s.sendMessage(responseForMe)
                }

                // отправляем другу информацию о том что он удален
                val dataForFriend = Cbor.encodeToByteArray(SocketBinaryMessage("i-was-deleted-from-friends", Cbor.encodeToByteArray(user.id)))
                val responseForFriend = BinaryMessage(dataForFriend)
                sessions[id]?.forEach { s ->
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
                sessions[user.id]?.forEach { s ->
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