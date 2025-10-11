package dev.koenv.rentmycar.plugins

import dev.koenv.rentmycar.routes.registerAllRoutes
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        registerAllRoutes(this)
    }
}
