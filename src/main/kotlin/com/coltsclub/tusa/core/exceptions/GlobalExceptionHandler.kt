package com.coltsclub.tusa.core.exceptions

import io.jsonwebtoken.ExpiredJwtException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authorization.AuthorizationDeniedException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest


@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(Exception::class)
    fun handleException(
        ex: Exception, request: WebRequest
    ): ResponseEntity<String> {
        when (ex) {
            is BadCredentialsException -> return ResponseEntity(ex.message, HttpStatus.FORBIDDEN)
            is UsernameNotFoundException -> return ResponseEntity(ex.message, HttpStatus.NOT_FOUND)
            is ExpiredJwtException -> return ResponseEntity(ex.message, HttpStatus.BAD_REQUEST)
            is AuthorizationDeniedException -> return ResponseEntity(ex.message, HttpStatus.UNAUTHORIZED)
        }
        return ResponseEntity(ex.message, HttpStatus.BAD_REQUEST)
    }
}
