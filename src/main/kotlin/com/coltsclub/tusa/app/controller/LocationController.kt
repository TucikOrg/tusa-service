package com.coltsclub.tusa.app.controller

import com.coltsclub.tusa.app.dto.AddLocationDto
import com.coltsclub.tusa.app.entity.LocationEntity
import com.coltsclub.tusa.app.repository.LocationRepository
import java.util.Optional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class LocationController(
    val locationRepository: LocationRepository
) {
    @PostMapping("/location")
    fun addLocation(addLocation: AddLocationDto) {
        val entity = LocationEntity(
            identifier = addLocation.identifier,
            latitude = addLocation.latitude,
            longitude = addLocation.longitude
        )
        locationRepository.save(entity)
    }

    @GetMapping("/location")
    fun getLocations(identifier: String): Optional<LocationEntity> {
        return locationRepository.findTopByIdentifierAndByOrderByCreationDesc(identifier)
    }
}