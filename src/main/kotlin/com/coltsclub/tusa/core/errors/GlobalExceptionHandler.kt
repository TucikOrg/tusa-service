package com.coltsclub.tusa.core.errors

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
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
        }
        return ResponseEntity(ex.message, HttpStatus.BAD_REQUEST)
    }
}
