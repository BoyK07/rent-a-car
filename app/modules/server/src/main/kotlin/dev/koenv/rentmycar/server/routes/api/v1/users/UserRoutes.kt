package dev.koenv.rentmycar.server.routes.api.v1.users

import dev.koenv.rentmycar.server.domain.service.UserService
import dev.koenv.rentmycar.server.mappers.user.toDto
import dev.koenv.rentmycar.server.routes.RouteRegistrar
import dev.koenv.rentmycar.server.util.getRole
import dev.koenv.rentmycar.server.util.getUserId
import dev.koenv.rentmycar.server.util.requireRole
import dev.koenv.rentmycar.server.util.requireUuidParamOrFail
import dev.koenv.rentmycar.shared.domain.enums.Role
import dev.koenv.rentmycar.shared.http.ApiException
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

object UserRoutes : RouteRegistrar {
    override fun Route.register() {
        val userService by inject<UserService>()

        route("/users") {
            authenticate("auth-jwt") {
                get {
                    call.requireRole(Role.ADMIN)
                    call.respond(userService.getAll().map { it.toDto() })
                }

                get("/{id}") {
                    val principal = call.requireRole(Role.ADMIN, Role.DRIVER, Role.MEMBER)
                    val requestedId = call.requireUuidParamOrFail("id")
                    val currentUserId = principal.getUserId()
                    val role = principal.getRole()

                    // Non-admin users can only view their own profile
                    if (role != Role.ADMIN && requestedId != currentUserId) {
                        throw ApiException(HttpStatusCode.Forbidden, message = "You can only view your own profile")
                    }

                    val user = userService.getById(requestedId)
                        ?: throw ApiException(HttpStatusCode.NotFound, message = "User not found")
                    call.respond(user.toDto())
                }

                delete("/{id}") {
                    call.requireRole(Role.ADMIN)
                    val id = call.requireUuidParamOrFail("id")
                    if (userService.delete(id)) call.respond(HttpStatusCode.NoContent)
                    else call.respond(HttpStatusCode.NotFound)
                }
            }
        }
    }
}