package dev.koenv.rentmycar.server.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.defaultheaders.*

fun Application.configureHTTP() {
    install(Compression)

    val allowedOrigins = environment.config.propertyOrNull("ktor.cors.allowedOrigins")
        ?.getString()
        ?.split(",")
        ?.map { it.trim() }
        ?: listOf("http://localhost:3000", "http://localhost:8081") // dev defaults for web + mobile

    install(CORS) {
        // Allowed HTTP methods
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)

        // Allowed headers
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Accept)

        // Configure allowed origins from environment
        allowedOrigins.forEach { origin ->
            val host = origin
                .removePrefix("http://")
                .removePrefix("https://")
                .split(":")[0] // Remove port if present

            // Ktor's allowHost handles schemes automatically
            allowHost(host, schemes = listOf("http", "https"))
        }

        allowCredentials = true
        maxAgeInSeconds = 3600
    }

    install(DefaultHeaders) {
        header("X-Engine", "Ktor")
    }
}
