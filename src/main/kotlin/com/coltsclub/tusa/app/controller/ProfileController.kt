package com.coltsclub.tusa.app.controller

import com.coltsclub.tusa.core.repository.UserRepository
import org.springframework.security.access.annotation.Secured
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@Secured("ROLE_USER")
class ProfileController(
    private val userRepository: UserRepository
) {
    @PutMapping("api/v1/profile/userUniqueName")
    fun changeUsername(@RequestBody uniqueName: String): Boolean {
        val phone = SecurityContextHolder.getContext().authentication.name
        val exist = userRepository.findByUserUniqueName(uniqueName).isPresent
        if (exist) {
            return false
        }
        val user = userRepository.findByPhone(phone).get()
        user.userUniqueName = uniqueName
        userRepository.save(user)
        return true
    }
    
    @PutMapping("api/v1/profile/name")
    fun changeName(@RequestBody name: String) {
        val phone = SecurityContextHolder.getContext().authentication.name
        val user = userRepository.findByPhone(phone).get()
        user.name = name
        userRepository.save(user)
    }

    @GetMapping("api/v1/profile/userUniqueName/available")
    fun isUserUniqueNameAvailable(@RequestParam name: String): Boolean {
        return !userRepository.findByUserUniqueName(name).isPresent
    }
}