package com.coltsclub.tusa.core.configuration

import com.coltsclub.tusa.core.repository.TokenRepository
import com.coltsclub.tusa.core.service.JwtService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService,
    private val userDetailsService: UserDetailsService,
    private val tokenRepository: TokenRepository
) : OncePerRequestFilter() {
    private val logger: Logger = LoggerFactory.getLogger(JwtAuthenticationFilter::class.java)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        if (request.servletPath.contains("api/v1/auth")) {
            filterChain.doFilter(request, response)
            return
        }

        val authHeader = request.getHeader("Authorization")
        if (authHeader.isNullOrEmpty() || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }

        val jwt = authHeader.substring(7)
        if (jwt.isBlank()) {
            return
        }

        try {
            val userId = jwtService.extractUsername(jwt)
            if (userId != null && SecurityContextHolder.getContext().authentication == null) {
                val userDetails: UserDetails = this.userDetailsService.loadUserByUsername(userId)
                val tokenEntity = tokenRepository.findByToken(jwt).get()
                val isTokenValid = !tokenEntity.expired && !tokenEntity.revoked
                if (isTokenValid) {
                    val authToken = UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.authorities
                    )
                    authToken.details = WebAuthenticationDetailsSource().buildDetails(request)
                    SecurityContextHolder.getContext().authentication = authToken
                }
            }
        } catch (usernameNotFoundException: UsernameNotFoundException) {
            logger.info("User not found")
        } catch (exception: Exception) {
            logger.info("Not determined error")
        }

        filterChain.doFilter(request, response)
    }
}