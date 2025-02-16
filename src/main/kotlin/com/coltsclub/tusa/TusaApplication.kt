package com.coltsclub.tusa

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import java.io.FileInputStream
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration
import org.springframework.boot.runApplication


@SpringBootApplication(exclude = [ErrorMvcAutoConfiguration::class])
class TusaApplication

fun main(args: Array<String>) {
	val serviceAccount = FileInputStream("tucik-e0058-firebase-adminsdk-fbsvc-930035d462.json")

	val options = FirebaseOptions.Builder()
		.setCredentials(GoogleCredentials.fromStream(serviceAccount))
		.build()
	FirebaseApp.initializeApp(options)

	runApplication<TusaApplication>(*args)
}
