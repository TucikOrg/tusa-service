package com.coltsclub.tusa.core.service

import com.coltsclub.tusa.core.entity.SmsCodeEntity
import com.coltsclub.tusa.core.repository.SmsCodeRepository
import java.time.LocalDateTime
import java.util.Base64
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

@Service
class SmsService(
    private val restTemplate: RestTemplate,
    private val smsCodeRepository: SmsCodeRepository
) {
    private val logger = org.slf4j.LoggerFactory.getLogger(SmsService::class.java)

    @Value("\${app.sms.login}")
    private lateinit var login: String

    @Value("\${app.sms.password}")
    private lateinit var password: String

    @Value("\${app.sms.url}")
    private lateinit var sendMessageUrl: String

    @Value("\${app.sms.send.real.sms}")
    private var sendRealSms: Boolean = false

    fun sendCodeSms(phone: String): Boolean {
        var success = true
        val code = (1000..9999).random().toString()
        logger.info("Send sms with code $code to $phone")

        if (sendRealSms) {
            val headers = org.springframework.http.HttpHeaders()
            headers.set("Authorization", "Basic ${Base64.getEncoder().encodeToString("$login:$password".toByteArray())}")

            val uri = UriComponentsBuilder.fromHttpUrl(sendMessageUrl)
                .queryParam("phone", phone)
                .queryParam("text", "Ваш код подтверждения: $code")
                .build().toUri()
            val entity = HttpEntity<String>(headers)
            val response: ResponseEntity<String> = restTemplate.exchange(uri, HttpMethod.GET, entity, String::class.java)
            val body = response.body
            success = body?.contains("accepted")?: false
        }

        if (success) {
            smsCodeRepository.save(SmsCodeEntity(phone, code, LocalDateTime.now().plusMinutes(5)))
        }

        return success
    }
}