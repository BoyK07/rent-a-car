package dev.koenv.rentmycar.routes.api.v1.users

import dev.koenv.rentmycar.domain.entity.Role
import dev.koenv.rentmycar.domain.service.UserService
import dev.koenv.rentmycar.mappers.user.toDto
import dev.koenv.rentmycar.routes.RouteRegistrar
import dev.koenv.rentmycar.shared.util.requireRole
import dev.koenv.rentmycar.shared.util.requireUuidParamOrFail
import dev.koenv.rentmycar.storage.repository.UserRepositoryImpl
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.route

object UserRoutes : RouteRegistrar {
    override fun Route.register() {
        val service = UserService(UserRepositoryImpl())

        route("/users") {
            authenticate("auth-jwt") {
                get {
                    call.requireRole(Role.ADMIN)
                    call.respond(service.getAll().map { it.toDto() })
                }

                get("/{id}") {
                    call.requireRole(Role.ADMIN, Role.DRIVER)
                    val id = call.requireUuidParamOrFail("id")
                    val user = service.getById(id)
                    if (user == null) call.respond(HttpStatusCode.NotFound)
                    else call.respond(user.toDto())
                }

                delete("/{id}") {
                    call.requireRole(Role.ADMIN)
                    val id = call.requireUuidParamOrFail("id")
                    if (service.delete(id)) call.respond(HttpStatusCode.NoContent)
                    else call.respond(HttpStatusCode.NotFound)
                }
            }
        }
    }
}