package com.coltsclub.tusa.core.service

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import java.security.Key
import java.util.Date
import java.util.HashMap
import java.util.function.Function

import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service


@Service
class JwtService {
    @Value("\${app.security.jwt.secret.key}")
    private val secretKey: String? = null

    fun extractUsername(token: String): String? {
        val claims: Claims = extractAllClaims(token)
        return claims.subject
    }

    fun generateToken(userDetails: UserDetails): String {
        return generateToken(HashMap(), userDetails)
    }

    fun generateToken(
        extraClaims: Map<String, Any>,
        userDetails: UserDetails
    ): String {
        return buildToken(extraClaims, userDetails)
    }

    private fun buildToken(
        extraClaims: Map<String, Any>,
        userDetails: UserDetails
    ): String {
        return Jwts
            .builder()
            .setClaims(extraClaims)
            .setSubject(userDetails.username)
            .setIssuedAt(Date(System.currentTimeMillis()))
            .signWith(signInKey, SignatureAlgorithm.HS256)
            .compact()
    }

    private fun extractAllClaims(token: String): Claims {
        return Jwts
            .parserBuilder()
            .setSigningKey(signInKey)
            .build()
            .parseClaimsJws(token)
            .body
    }

    private val signInKey: Key
        get() {
            val keyBytes: ByteArray = Decoders.BASE64.decode(secretKey)
            return Keys.hmacShaKeyFor(keyBytes)
        }
}