package com.coltsclub.tusa.app.controller

import org.springframework.core.io.ResourceLoader
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class LegalController(
    private val resourceLoader: ResourceLoader
) {
    @GetMapping("api/v1/legal/documents")
    fun getLegalDocumentsText(): String {
        return resourceLoader.getResource("classpath:static/author-legal-message.html").getContentAsString(Charsets.UTF_8)
    }
}