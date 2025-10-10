package dev.koenv.rentmycar.api.v1.users

import dev.koenv.rentmycar.domain.entity.Role
import dev.koenv.rentmycar.domain.entity.User
import dev.koenv.rentmycar.domain.service.UserService
import dev.koenv.rentmycar.dto.toResponse
import dev.koenv.rentmycar.shared.util.requireRole
import dev.koenv.rentmycar.shared.util.requireUuidParamOrFail
import dev.koenv.rentmycar.storage.repository.UserRepositoryImpl
import io.ktor.http.*
import io.ktor.server.auth.authenticate
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.userRoutes() {
    val service = UserService(UserRepositoryImpl())

    route("/users") {
        authenticate("auth-jwt") {
            get {
                call.requireRole(Role.USER)
                val users = service.getAll()
                call.respond(users.map { it.toResponse() })
            }
        }
        post {
            val user = call.receive<User>()
            val registeredUser = service.register(user)
            call.respond(HttpStatusCode.Created, registeredUser)
        }

        get("/{id}") {
            val id = call.requireUuidParamOrFail("id")
            val user = service.getById(id)
            if (user == null) call.respond(HttpStatusCode.NotFound) else call.respond(user)
        }

        put("/{id}") {
            TODO("Not yet implemented")
//            val id = call.requireUuidParamOrFail("id")
//            val body = call.requireBodyOrFail<User>()
//            val updated = try { service.update(id, body) } catch (e: Exception) {
//                return@put call.respond(HttpStatusCode.NotFound)
//            }
//            call.respond(updated)
        }

        delete("/{id}") {
            TODO("Not yet implemented")
//            val idParam = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest, "Missing id")
//            val id = try {
//                UUID.fromString(idParam)
//            } catch (e: IllegalArgumentException) {
//                return@delete call.respond(HttpStatusCode.BadRequest, "Invalid id")
//            }
//
//            val deleted = service.delete(id)
//            if (deleted) call.respond(HttpStatusCode.NoContent) else call.respond(HttpStatusCode.NotFound)
        }
    }
}