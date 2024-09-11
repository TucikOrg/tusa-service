package com.coltsclub.tusa.core.configuration

import com.coltsclub.tusa.core.exceptions.TucikBadRequest
import com.coltsclub.tusa.core.repository.SmsCodeRepository
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component


@Component
class SmsCodeAuthenticationProvider(
    private val smsCodeRepository: SmsCodeRepository
) : AuthenticationProvider {
    override fun authenticate(authentication: Authentication): Authentication {
        val phone = authentication.name
        val code = authentication.credentials.toString()

        val loginCode = smsCodeRepository.findLoginCode(phone, code)
            .orElseThrow { throw TucikBadRequest("Invalid code") }
        smsCodeRepository.save(loginCode.apply { activated = true })

        return UsernamePasswordAuthenticationToken(phone, code, ArrayList())
    }

    override fun supports(authentication: Class<*>): Boolean {
        return authentication == UsernamePasswordAuthenticationToken::class.java;
    }
}