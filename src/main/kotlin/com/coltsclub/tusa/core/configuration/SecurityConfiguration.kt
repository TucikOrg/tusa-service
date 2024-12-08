package com.coltsclub.tusa.core.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
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
    private val authenticationProvider: TucikAuthenticationProvider,
    private val logoutHandler: LogoutHandler
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { csrf -> csrf.disable() }
            .authorizeHttpRequests { req ->
                req.anyRequest().permitAll()
//                req.requestMatchers(
//                    "swagger-ui/**",
//                    "v3/api-docs/**",
//                    "api/v1/auth/**",
//                    "api/v1/avatar",
//                    "api/v1/avatar/**",
//                    "api/v1/legal/**",
//                    "api/v1/location/**",
//                    "/"
//                )
//                    .permitAll()
//                    .anyRequest()
//                    .authenticated()
            }
            .sessionManagement { session -> session.sessionCreationPolicy(STATELESS) }
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)
            .logout { logout ->
                logout.logoutUrl("api/v1/logout")
                    .addLogoutHandler(logoutHandler)
                    .logoutSuccessHandler { request, response, authentication ->
                        SecurityContextHolder.clearContext()
                    }
            }
        return http.build()
    }
}