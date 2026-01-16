package dev.koenv.rentmycar.server.routes.root

/**
 * Root-level HTTP routes.
 * 
 * Endpoints:
 * - GET /health - Health check endpoint (returns "OK")
 */

import dev.koenv.rentmycar.server.routes.RouteRegistrar
import io.ktor.server.response.*
import io.ktor.server.routing.*

object RootRoutes : RouteRegistrar {
    override fun Route.register() {
        get("/health") { call.respondText("OK") }
    }
}
