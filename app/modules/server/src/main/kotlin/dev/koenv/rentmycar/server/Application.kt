package dev.koenv.rentmycar.server

import dev.koenv.rentmycar.server.plugins.*
import io.ktor.server.application.*
import io.ktor.server.netty.*

/**
 * Application entry point for the Rent My Car backend server.
 * 
 * This Ktor-based server provides a REST API for the car rental platform,
 * including user management, car listings, reservations, and driving sessions.
 */
fun main(args: Array<String>) {
    EngineMain.main(args)
}

/**
 * Main application module that configures all server plugins and features.
 * 
 * Configuration order is important:
 * 1. HTTP (CORS, compression, etc.)
 * 2. Serialization (JSON support)
 * 3. Dependency Injection (Koin)
 * 4. Database (connection and migrations)
 * 5. Monitoring (metrics and health checks)
 * 6. Administration (management endpoints)
 * 7. Security (authentication and authorization)
 * 8. Error Handling (exception mapping)
 * 9. Routing (API endpoints)
 */
fun Application.module() {
    configureHTTP()
    configureSerialization()
    configureDI()
    configureDatabase()
    configureMonitoring()
    configureAdministration()
    configureSecurity()
    configureErrorHandling()
    configureRouting()
}

