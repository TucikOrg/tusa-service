package com.coltsclub.tusa.core.service

import com.coltsclub.tusa.core.entity.TokenEntity
import com.coltsclub.tusa.core.entity.UserEntity
import com.coltsclub.tusa.core.enums.Role
import com.coltsclub.tusa.core.enums.TokenType
import com.coltsclub.tusa.core.repository.SmsCodeRepository
import com.coltsclub.tusa.core.repository.TokenRepository
import com.coltsclub.tusa.core.repository.UserRepository
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.stereotype.Service

@Service
class AuthenticationService(
    private val authenticationManager: AuthenticationManager,
    private val userRepository: UserRepository,
    private val jwtService: JwtService,
    private val tokenRepository: TokenRepository,
    private val smsCodeRepository: SmsCodeRepository
) {
    fun authenticate(phone: String, code: String): String {
        val loginCode = smsCodeRepository.findLoginCode(phone, code)
            .orElseThrow { throw IllegalArgumentException("Invalid code") }
        smsCodeRepository.save(loginCode.apply { activated = true })

        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(
                phone,
                code
            )
        )
        val user = userRepository.findByPhone(phone).orElseGet {
            userRepository.save(UserEntity("", phone, "", Role.USER))
        }

        val jwtToken = jwtService.generateToken(user)
        saveUserToken(user, jwtToken)
        return jwtToken
    }

    fun saveUserToken(user: UserEntity, jwtToken: String) {
        val token = TokenEntity(jwtToken, TokenType.BEARER, user = user)
        tokenRepository.save<TokenEntity>(token)
    }
}