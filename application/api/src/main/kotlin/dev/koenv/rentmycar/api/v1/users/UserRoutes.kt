package dev.koenv.rentmycar.api.v1.users

import dev.koenv.rentmycar.domain.model.User
import dev.koenv.rentmycar.plugins.userService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.userRoutes() {
    val service = application.userService()

    route("/users") {
        post {
            val user = call.receive<User>()
            val id = service.create(user)
            call.respond(HttpStatusCode.Created, id)
        }

        get("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid ID")
            val user = service.read(id) ?: return@get call.respond(HttpStatusCode.NotFound)
            call.respond(user)
        }

        put("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@put call.respond(HttpStatusCode.BadRequest, "Invalid ID")
            val user = call.receive<User>()
            val ok = service.update(id, user)
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
