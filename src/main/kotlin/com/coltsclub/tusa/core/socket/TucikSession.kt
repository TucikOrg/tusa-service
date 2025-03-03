package com.coltsclub.tusa.core.socket

import org.springframework.web.socket.WebSocketSession

data class TucikSession(
    val webSocketSession: WebSocketSession,
    var lastResponseTime: Long = System.currentTimeMillis()
)