package com.coltsclub.tusa.core.controller

import com.coltsclub.tusa.core.dto.LoginResponseDto
import com.coltsclub.tusa.core.service.AuthenticationService
import com.google.api.client.auth.openidconnect.IdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import java.util.Collections
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController


@RestController
class AuthenticationController(
    private val authenticationService: AuthenticationService
) {
    @Value("\${app.google.client.id}")
    private lateinit var googleClientId: String
    private val logger: Logger = LoggerFactory.getLogger(AuthenticationController::class.java)

    @PostMapping("api/v1/auth/google/login")
    fun googleLogin(@RequestBody idTokenString: String): LoginResponseDto {
        val googleVerifier: GoogleIdTokenVerifier = GoogleIdTokenVerifier.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance()
        )
            .setAudience(Collections.singletonList(googleClientId))
            .build()

        try {
            val idToken: GoogleIdToken? = googleVerifier.verify(idTokenString)
            if (idToken != null) {
                val payload: IdToken.Payload = idToken.payload
                val userId: String = payload.subject
                logger.info("User ID: $userId")

                // Get profile information from payload
                val email: String = payload["email"] as String
                val name = payload["name"] as String
                val pictureUrl = payload["picture"] as String

                val response = authenticationService.authenticate(
                    gmail = email,
                    pictureUrl = pictureUrl,
                    name = name
                )
                return response
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
        }


        throw BadCredentialsException("Invalid google token")
    }
}