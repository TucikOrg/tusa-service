package com.coltsclub.tusa.app.controller

import com.coltsclub.tusa.app.dto.AddLocationDto
import com.coltsclub.tusa.app.entity.LocationEntity
import com.coltsclub.tusa.app.repository.LocationRepository
import com.coltsclub.tusa.app.service.LocationService
import com.coltsclub.tusa.core.service.EncryptionService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
@PreAuthorize("hasRole('USER')")
class LocationController(
    val locationRepository: LocationRepository,
    val encryptionService: EncryptionService,
    val locationService: LocationService
) {
    @PostMapping("api/v1/location/add")
    fun addLocation(@RequestBody addLocation: AddLocationDto) {
        val userId = SecurityContextHolder.getContext().authentication.name.toLong()
        val secretKey = locationService.getSecretKey()
        val encLatitude = encryptionService.encrypt(addLocation.latitude.toString(), secretKey)
        val encLongitude = encryptionService.encrypt(addLocation.longitude.toString(), secretKey)
        val entity = LocationEntity(
            ownerId = userId,
            latitude = encLatitude,
            longitude = encLongitude,
        )
        locationRepository.save(entity)
    }
}