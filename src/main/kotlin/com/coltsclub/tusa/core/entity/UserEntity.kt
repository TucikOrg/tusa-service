package com.coltsclub.tusa.core.entity

import com.coltsclub.tusa.core.enums.Role
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

@Entity(name = "appUser")
data class UserEntity(
    @Column(unique = true) var userUniqueName: String?,
    val phone: String,
    var name: String,
    @Enumerated(EnumType.STRING) val role: Role,
    var gmail: String,
    var firebaseToken: String?
) : UserDetails {
    @Id
    @GeneratedValue
    val id: Long? = null

    override fun getAuthorities(): List<SimpleGrantedAuthority> {
        return role.getAuthorities()
    }

    override fun getPassword() = ""

    override fun getUsername() = id.toString()

    override fun isAccountNonExpired() = true

    override fun isAccountNonLocked() = true

    override fun isCredentialsNonExpired() = true

    override fun isEnabled() = true
}

