package com.coltsclub.tusa.app.controller

import com.coltsclub.tusa.app.dto.AddLocationDto
import com.coltsclub.tusa.app.entity.LocationEntity
import com.coltsclub.tusa.app.repository.LocationRepository
import java.util.Optional
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
@PreAuthorize("hasRole('USER')")
class LocationController(
    val locationRepository: LocationRepository
) {
    @PostMapping("api/v1/location/add")
    fun addLocation(@RequestBody addLocation: AddLocationDto) {
        val phone = SecurityContextHolder.getContext().authentication.name

        val entity = LocationEntity(
            phone = phone,
            latitude = addLocation.latitude,
            longitude = addLocation.longitude,
        )
        locationRepository.save(entity)
    }

    @GetMapping("api/v1/location/last")
    fun getLocations(phone: String): Optional<LocationEntity> {
        return locationRepository.findTopByPhoneAndByOrderByCreationDesc(phone)
    }
}