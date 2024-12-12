package com.coltsclub.tusa.app.service

import com.coltsclub.tusa.core.entity.UserEntity
import com.coltsclub.tusa.core.enums.Role
import com.coltsclub.tusa.core.exceptions.TucikBadRequest
import com.coltsclub.tusa.core.repository.UserRepository
import java.util.Optional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class ProfileService(
    private val userRepository: UserRepository
) {
    fun getUsers(uniqueName: String, page: Pageable): Page<UserEntity> {
        return userRepository.findByUserUniqueNameContaining(uniqueName, page)
    }

    fun isUserUniqueNameAvailable(uniqueName: String): Boolean {
        return !userRepository.findByUserUniqueName(uniqueName).isPresent
    }

    fun changeName(userId: Long, name: String) {
        val user = userRepository.findById(userId).get()
        user.name = name
        userRepository.save(user)
    }

    fun getUser(gmail: String): Optional<UserEntity> {
        return userRepository.findByGmail(gmail)
    }

    fun changeUniqueName(userId: Long, newUniqueName: String): Boolean {
        val exist = userRepository.findByUserUniqueName(newUniqueName).isPresent
        if (exist) {
            return false
        }
        val user = userRepository.findById(userId).get()
        user.userUniqueName = newUniqueName
        userRepository.save(user)
        return true
    }

    fun createUser(uniqueName: String?, gmail: String?, name: String?): UserEntity? {
        if (uniqueName != null && !isUserUniqueNameAvailable(uniqueName)) return null

        return userRepository.save(
            UserEntity(
                userUniqueName = uniqueName,
                phone = "",
                name = name,
                role = Role.USER,
                gmail = gmail
            )
        )
    }
}