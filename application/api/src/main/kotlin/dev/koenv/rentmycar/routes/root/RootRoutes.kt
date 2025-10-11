package dev.koenv.rentmycar.routes.root

import dev.koenv.rentmycar.routes.RouteRegistrar
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Routes for the root scope: `/`, `/health`, `/docs`, etc.
 */
object RootRoutes : RouteRegistrar {
    override fun Route.register() {
        route("/") {
            get("/health") { call.respondText("OK") }
            get("/docs") { call.respondText("API documentation placeholder") }
        }
    }
}
