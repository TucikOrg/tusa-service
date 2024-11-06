package com.coltsclub.tusa.core.configuration

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class SwaggerConfiguration {
    private fun createAPIKeyScheme(): SecurityScheme {
        return SecurityScheme().type(SecurityScheme.Type.HTTP)
            .bearerFormat("JWT")
            .scheme("bearer")
    }

    @Bean
    fun openAPI(): OpenAPI {
        return OpenAPI()
            .addServersItem(Server())
            .addServersItem(Server().url("http://192.168.0.103:8080"))
            .addServersItem(Server().url("https://tucik.fun"))
            .addServersItem(Server().url("http://89.111.174.188:8080"))
            .addSecurityItem(SecurityRequirement().addList("BearerAuthentication"))
            .components(Components().addSecuritySchemes("BearerAuthentication", createAPIKeyScheme()))
            .info(
                Info().title("Tucik API")
                    .description("Tucik social network api.")
                    .version("1.0").contact(
                        Contact().name("Artem Bobkin")
                            .email("artembobkincolt@gmail.com")
                    )
            )
    }
}