package com.coltsclub.tusa.core.service

import com.coltsclub.tusa.core.entity.TokenEntity
import com.coltsclub.tusa.core.entity.UserEntity
import com.coltsclub.tusa.core.enums.Role
import com.coltsclub.tusa.core.enums.TokenType
import com.coltsclub.tusa.core.model.AuthenticateInstruction
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
    private val tokenRepository: TokenRepository
) {
    fun authenticate(phone: String, code: String, device: String): AuthenticateInstruction {
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(
                phone,
                code
            )
        )

        var needTransferLocationToken = false
        val userOpt = userRepository.findByPhone(phone)
        val user = if (userOpt.isPresent) {
            userOpt.get()
        } else {
            userRepository.save(UserEntity("", phone, "", Role.USER, devices = listOf(device)))
        }

        needTransferLocationToken = user.devices.contains(device).not()

        val jwtToken = jwtService.generateToken(user)
        saveUserToken(user, jwtToken)
        return AuthenticateInstruction(jwtToken, needTransferLocationToken)
    }

    fun saveUserToken(user: UserEntity, jwtToken: String) {
        val token = TokenEntity(jwtToken, TokenType.BEARER, user = user)
        tokenRepository.save<TokenEntity>(token)
    }
}