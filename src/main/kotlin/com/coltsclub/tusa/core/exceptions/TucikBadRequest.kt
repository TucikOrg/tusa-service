package com.coltsclub.tusa.core.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.BAD_REQUEST)
class TucikBadRequest(message: String) : RuntimeException(message)