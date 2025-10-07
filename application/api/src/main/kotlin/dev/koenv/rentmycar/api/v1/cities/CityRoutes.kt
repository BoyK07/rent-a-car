package dev.koenv.rentmycar.api.v1.cities

import dev.koenv.rentmycar.domain.model.City
import dev.koenv.rentmycar.plugins.cityService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.cityRoutes() {
    val service = application.cityService()

    route("/cities") {
        post {
            val city = call.receive<City>()
            val id = service.create(city)
            call.respond(HttpStatusCode.Created, id)
        }

        get("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid ID")
            val city = service.read(id) ?: return@get call.respond(HttpStatusCode.NotFound)
            call.respond(city)
        }

        put("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@put call.respond(HttpStatusCode.BadRequest, "Invalid ID")
            val city = call.receive<City>()
            val ok = service.update(id, city)
            call.respond(if (ok) HttpStatusCode.OK else HttpStatusCode.NotFound)
        }

        delete("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest, "Invalid ID")
            val ok = service.delete(id)
            call.respond(if (ok) HttpStatusCode.OK else HttpStatusCode.NotFound)
        }
    }
}
