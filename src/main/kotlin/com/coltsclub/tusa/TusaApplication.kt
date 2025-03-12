package com.coltsclub.tusa

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration
import org.springframework.boot.runApplication
/////

@SpringBootApplication(exclude = [ErrorMvcAutoConfiguration::class])
class TusaApplication

fun main(args: Array<String>) {
	runApplication<TusaApplication>(*args)
}
