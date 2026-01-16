package dev.koenv.rentmycar.server.plugins

import dev.koenv.rentmycar.server.routes.registerAllRoutes
import io.ktor.server.application.*
import io.ktor.server.resources.*
import io.ktor.server.routing.*

/**
 * Configures application routing.
 * 
 * Installs:
 * - Resources plugin for type-safe routing
 * - All application routes via registerAllRoutes()
 * 
 * Routes are organized hierarchically and include:
 * - Root endpoints (health, status)
 * - API v1 endpoints (auth, users, cars, reservations, etc.)
 */
fun Application.configureRouting() {
    install(Resources)
    
    routing {
        registerAllRoutes(this)
    }
}
