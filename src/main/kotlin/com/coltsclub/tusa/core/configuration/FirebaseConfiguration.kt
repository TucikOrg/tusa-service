package com.coltsclub.tusa.core.configuration

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import java.io.FileInputStream
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class FirebaseConfiguration {
    @Bean
    fun firebaseApp(): FirebaseApp {
        val serviceAccount = FileInputStream("tucik-e0058-firebase-adminsdk-fbsvc-930035d462.json")

        val options = FirebaseOptions.Builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .build()
        return FirebaseApp.initializeApp(options)
    }
}