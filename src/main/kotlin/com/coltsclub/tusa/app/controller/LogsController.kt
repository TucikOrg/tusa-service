package com.coltsclub.tusa.app.controller

import com.coltsclub.tusa.app.dto.CrashData
import com.coltsclub.tusa.app.entity.LogsEntity
import com.coltsclub.tusa.app.repository.LogsRepository
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class LogsController(
    private val logsRepository: LogsRepository
) {
    @PostMapping("api/v1/logs")
    fun createLog(@RequestBody crashData: CrashData) {
        var userId = 0L
        if (SecurityContextHolder.getContext().authentication != null) {
            userId = SecurityContextHolder.getContext().authentication.name.toLong()
        }

        logsRepository.save(LogsEntity(
            message = crashData.message,
            stackTrace = crashData.stackTrace,
            thread = crashData.thread,
            userId = userId
        ))
    }
}