package com.coltsclub.tusa.core.configuration

import com.coltsclub.tusa.core.enums.Role
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod.GET
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy.STATELESS
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.authentication.logout.LogoutHandler


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfiguration(
    private val jwtAuthFilter: JwtAuthenticationFilter,
    private val authenticationProvider: AuthenticationProvider,
    private val logoutHandler: LogoutHandler
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { csrf -> csrf.disable() }
            .authorizeHttpRequests { req ->
                req.requestMatchers("swagger-ui/**", "v3/api-docs/**", "api/v1/auth/**")
                    .permitAll()
                    .anyRequest().authenticated()
            }
            .sessionManagement { session -> session.sessionCreationPolicy(STATELESS) }
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)
            .logout { logout ->
                logout.logoutUrl("/logout")
                    .addLogoutHandler(logoutHandler)
                    .logoutSuccessHandler { request, response, authentication -> SecurityContextHolder.clearContext() }
            }
        return http.build()
    }
}