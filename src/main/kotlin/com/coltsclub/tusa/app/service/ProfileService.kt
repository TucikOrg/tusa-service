package com.coltsclub.tusa.app.service

import com.coltsclub.tusa.core.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class ProfileService(
    private val userRepository: UserRepository
) {
    fun isUserUniqueNameAvailable(uniqueName: String): Boolean {
        return !userRepository.findByUserUniqueName(uniqueName).isPresent
    }

    fun changeName(userId: Long, name: String) {
        val user = userRepository.findById(userId).get()
        user.name = name
        userRepository.save(user)
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
}