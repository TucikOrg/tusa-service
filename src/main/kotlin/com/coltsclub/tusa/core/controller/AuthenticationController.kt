package com.coltsclub.tusa.core.controller

import com.coltsclub.tusa.core.dto.LoginDto
import com.coltsclub.tusa.core.dto.LoginResponseDto
import com.coltsclub.tusa.core.service.AuthenticationService
import com.coltsclub.tusa.core.service.SmsService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class AuthenticationController(
    private val smsService: SmsService,
    private val authenticationService: AuthenticationService
) {
    @PostMapping("api/v1/auth/login")
    fun login(@RequestBody login: LoginDto): LoginResponseDto {
        return authenticationService.authenticate(login.phone, login.code)
    }

    @PostMapping("api/v1/auth/send-code")
    fun sendCode(@RequestBody phone: String) {
        smsService.sendCodeSms(phone)
    }
}