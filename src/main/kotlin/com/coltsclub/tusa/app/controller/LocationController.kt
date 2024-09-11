package com.coltsclub.tusa.app.controller

import com.coltsclub.tusa.app.dto.AddLocationDto
import com.coltsclub.tusa.app.dto.LocationDto
import com.coltsclub.tusa.app.entity.LocationEntity
import com.coltsclub.tusa.app.repository.LocationRepository
import com.coltsclub.tusa.core.exceptions.TucikBadRequest
import com.coltsclub.tusa.core.service.EncryptionService
import javax.crypto.SecretKey
import kotlin.jvm.optionals.getOrNull
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
@PreAuthorize("hasRole('USER')")
class LocationController(
    val locationRepository: LocationRepository,
    val encryptionService: EncryptionService
) {
    // value secret key
    @Value("\${app.tucik.secret-key}")
    private lateinit var secretKeyStr: String

    private var secretKey: SecretKey? = null

    private fun getSecretKey(): SecretKey {
        if (secretKey == null) {
            secretKey = encryptionService.stringToSecretKey(secretKeyStr)
        }
        return secretKey!!
    }

    @PostMapping("api/v1/location/add")
    fun addLocation(@RequestBody addLocation: AddLocationDto) {
        val phone = SecurityContextHolder.getContext().authentication.name

        val encLatitude = encryptionService.encrypt(addLocation.latitude.toString(), getSecretKey())
        val encLongitude = encryptionService.encrypt(addLocation.longitude.toString(), getSecretKey())
        val entity = LocationEntity(
            phone = phone,
            latitude = encLatitude,
            longitude = encLongitude,
        )
        locationRepository.save(entity)
    }

    @GetMapping("api/v1/location/last")
    fun getLastLocation(phone: String): LocationDto {
        val location = locationRepository.findTopByPhoneAndByOrderByCreationDesc(phone).getOrNull()?: throw TucikBadRequest("Can't get location")
        val longitude = encryptionService.decrypt(location.longitude, getSecretKey()).toFloat()
        val latitude = encryptionService.decrypt(location.latitude, getSecretKey()).toFloat()
        return LocationDto(
            location.phone,
            longitude = longitude,
            latitude = latitude
        )
    }
}