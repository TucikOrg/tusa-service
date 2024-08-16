package com.coltsclub.tusa.core.enums

import org.springframework.security.core.authority.SimpleGrantedAuthority

enum class Role {
    USER,
    ADMIN;

    fun getAuthorities(): List<SimpleGrantedAuthority> {
        return when (this) {
            USER -> listOf(SimpleGrantedAuthority("ROLE_USER"))
            ADMIN -> listOf(SimpleGrantedAuthority("ROLE_ADMIN"))
        }
    }
}