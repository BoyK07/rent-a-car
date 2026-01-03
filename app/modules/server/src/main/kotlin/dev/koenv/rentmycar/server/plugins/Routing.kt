package dev.koenv.rentmycar.server.plugins

import dev.koenv.rentmycar.server.routes.registerAllRoutes
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        registerAllRoutes(this)
    }
}
