package com.coltsclub.tusa.app.service

import com.coltsclub.tusa.app.dto.LocationDto
import com.coltsclub.tusa.app.entity.LocationEntity
import com.coltsclub.tusa.app.repository.LocationRepository
import com.coltsclub.tusa.core.service.EncryptionService
import java.time.LocalDateTime
import java.time.ZoneOffset
import javax.crypto.SecretKey
import kotlin.jvm.optionals.getOrNull
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class LocationService(
    val locationRepository: LocationRepository,
    val encryptionService: EncryptionService,
    val friendsService: FriendsService
) {
    // value secret key
    @Value("\${app.tucik.secret.key}")
    private lateinit var secretKeyStr: String

    private var secretKey: SecretKey? = null

    fun getSecretKey(): SecretKey {
        if (secretKey == null) {
            secretKey = encryptionService.stringToSecretKey(secretKeyStr)
        }
        return secretKey!!
    }

    fun getUsersLocations(usersIds: List<Long>): List<LocationDto> {
        val now = LocalDateTime.now(ZoneOffset.UTC)
        val locations = usersIds.map { id ->
            val location = locationRepository.findTopByOwnerIdAndByOrderByCreationDesc(id)
            if (location.isEmpty) {
                return@map null
            }
            if (location.get().hideMe == true) {
                return@map null
            }
            // слишком старая локация
            if (location.get().creation.plusDays(5) < now) {
                return@map null
            }
            return@map location.get()
        }

        return locations.filterNotNull().map { location ->
            val longitude = encryptionService.decrypt(location.longitude, getSecretKey()).toFloat()
            val latitude = encryptionService.decrypt(location.latitude, getSecretKey()).toFloat()
            LocationDto(
                location.ownerId,
                longitude = longitude,
                latitude = latitude
            )
        }
    }

    fun getLastLocationIfVisible(ownerId: Long): LocationDto? {
        val location = locationRepository.findTopByOwnerIdAndByOrderByCreationDesc(ownerId).getOrNull()?: return null
        if (location.hideMe == true) {
            return null
        }
        val longitude = encryptionService.decrypt(location.longitude, getSecretKey()).toFloat()
        val latitude = encryptionService.decrypt(location.latitude, getSecretKey()).toFloat()
        return LocationDto(
            location.ownerId,
            longitude = longitude,
            latitude = latitude
        )
    }

    fun fakeLocation(latitude: Float, longitude: Float, userId: Long) {
        val secretKey = getSecretKey()
        val encLatitude = encryptionService.encrypt(latitude.toString(), secretKey)
        val encLongitude = encryptionService.encrypt(longitude.toString(), secretKey)
        val entity = LocationEntity(
            ownerId = userId,
            latitude = encLatitude,
            longitude = encLongitude,
        )
        locationRepository.save(entity)
    }

    fun setVisibleLocationStateMe(userId: Long, visible: Boolean) {
        locationRepository.findTopByOwnerIdAndByOrderByCreationDesc(userId).getOrNull()?.let {
            it.hideMe = !visible
            locationRepository.save(it)
        }
    }
}