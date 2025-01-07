package com.coltsclub.tusa.core.configuration

import com.coltsclub.tusa.app.service.FriendsService
import com.coltsclub.tusa.app.service.AvatarService
import com.coltsclub.tusa.app.service.ChatsService
import com.coltsclub.tusa.app.service.LocationService
import com.coltsclub.tusa.app.service.MessagesService
import com.coltsclub.tusa.app.service.ProfileService
import com.coltsclub.tusa.core.socket.WebSocketHandler
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

@Configuration
@EnableWebSocket
class WebSocketConfig(
    private val friendsService: FriendsService,
    private val avatarService: AvatarService,
    private val profileService: ProfileService,
    private val locationService: LocationService,
    private val messagesService: MessagesService,
    private val chatService: ChatsService
): WebSocketConfigurer {
    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(
            WebSocketHandler(
                friendsService,
                profileService,
                avatarService,
                locationService,
                chatService,
                messagesService,
            ), "/stream"
        ).setAllowedOrigins("*")
    }
}