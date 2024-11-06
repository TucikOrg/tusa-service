package com.coltsclub.tusa.core.service

import com.coltsclub.tusa.core.dto.LoginResponseDto
import com.coltsclub.tusa.core.entity.TokenEntity
import com.coltsclub.tusa.core.entity.UserEntity
import com.coltsclub.tusa.core.enums.Role
import com.coltsclub.tusa.core.enums.TokenType
import com.coltsclub.tusa.core.repository.TokenRepository
import com.coltsclub.tusa.core.repository.UserRepository
import java.util.Optional
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
    fun authenticate(
        gmail: String?,
        pictureUrl: String?,
        name: String?
    ): LoginResponseDto {
        // Тут пользователь уже авторизован
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(
                gmail,
                "google"
            )
        )

        var userOpt = Optional.empty<UserEntity>()
        if (gmail != null) {
            userOpt = userRepository.findByGmail(gmail)
        }

        val user = if (userOpt.isPresent) {
            userOpt.get()
        } else {
            userRepository.save(UserEntity(
                userUniqueName = null,
                phone = "",
                name = name,
                role = Role.USER,
                gmail = gmail
            ))
        }

        val jwtToken = jwtService.generateToken(user)
        saveUserToken(user, jwtToken)

        return LoginResponseDto(
            uniqueName = user.userUniqueName?: "",
            name = user.name?: "",
            jwt = jwtToken,
            id = user.id!!,
            phone = user.phone?: "",
            gmail = user.gmail?: "",
            pictureUrl = pictureUrl?: ""
        )
    }

    fun saveUserToken(user: UserEntity, jwtToken: String) {
        val token = TokenEntity(jwtToken, TokenType.BEARER, user = user)
        tokenRepository.save<TokenEntity>(token)
    }
}