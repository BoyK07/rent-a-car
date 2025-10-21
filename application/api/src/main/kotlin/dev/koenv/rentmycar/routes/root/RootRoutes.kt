package dev.koenv.rentmycar.routes.root

import dev.koenv.rentmycar.routes.RouteRegistrar
import io.ktor.server.response.*
import io.ktor.server.routing.*

object RootRoutes : RouteRegistrar {
    override fun Route.register() {
        get("/health") { call.respondText("OK") }
    }
}
