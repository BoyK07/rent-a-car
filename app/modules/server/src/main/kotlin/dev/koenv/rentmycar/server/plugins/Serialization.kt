package dev.koenv.rentmycar.server.plugins

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json

/**
 * Configures JSON serialization for the application.
 * 
 * Settings:
 * - prettyPrint: false (compact JSON for production)
 * - ignoreUnknownKeys: true (tolerant of extra fields in requests)
 * - isLenient: true (allows non-strict JSON parsing)
 * - encodeDefaults: true (includes default values in responses)
 * 
 * Uses Kotlinx Serialization for type-safe JSON handling.
 */
fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(
            Json {
                prettyPrint = false
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true
            }
        )
    }
}
