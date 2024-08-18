package com.coltsclub.tusa.app.controller

import com.coltsclub.tusa.app.service.EncryptionService
import javax.crypto.SecretKey
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class EncryptionController(
    val encryptionService: EncryptionService
) {
    @PostMapping("api/v1/encryption/secret-key")
    fun generateSecretKey(): SecretKey {
        return encryptionService.generateKey()
    }
}