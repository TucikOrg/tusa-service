package com.coltsclub.tusa.app.controller

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TicketsController {

    @PostMapping("api/v1/tickets/add")
    fun createEvent() {

    }
}