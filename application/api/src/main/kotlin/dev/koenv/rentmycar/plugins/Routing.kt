package dev.koenv.rentmycar.plugins

import dev.koenv.rentmycar.api.v1.cities.cityRoutes
import dev.koenv.rentmycar.api.v1.users.userRoutes
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText(
                text = "500: ${cause.message ?: "Internal Server Error"}",
                status = HttpStatusCode.InternalServerError
            )
        }
        status(HttpStatusCode.NotFound) {
            call.respondText("Not Found", status = HttpStatusCode.NotFound)
        }
    }

    routing {
        get("/") { call.respondText("OK") }

        route("/api/v1") {
            cityRoutes()
            userRoutes()
        }
    }
}

