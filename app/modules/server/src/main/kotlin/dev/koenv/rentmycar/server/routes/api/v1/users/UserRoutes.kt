package dev.koenv.rentmycar.server.routes.api.v1.users

/**
 * User management API routes.
 * 
 * All endpoints require authentication.
 * 
 * Endpoints:
 * - GET /api/v1/users/{id} - Get user by ID (own profile or admin only)
 * - PATCH /api/v1/users/{id} - Update user (own profile or admin only)
 * - DELETE /api/v1/users/{id} - Delete user (admin only)
 * - GET /api/v1/users - List all users (admin only)
 */

import dev.koenv.rentmycar.server.domain.service.UserService
import dev.koenv.rentmycar.server.mappers.user.toDto
import dev.koenv.rentmycar.server.routes.RouteRegistrar
import dev.koenv.rentmycar.server.util.getRole
import dev.koenv.rentmycar.server.util.getUserId
import dev.koenv.rentmycar.server.util.requireRole
import dev.koenv.rentmycar.server.util.respondSuccess
import dev.koenv.rentmycar.server.util.respondError
import dev.koenv.rentmycar.shared.domain.enums.Role
import dev.koenv.rentmycar.shared.http.ApiException
import dev.koenv.rentmycar.shared.resources.ApiV1
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.resources.get
import io.ktor.server.resources.delete
import io.ktor.server.resources.patch
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import kotlin.uuid.Uuid
import dev.koenv.rentmycar.shared.dto.user.PatchUserRequestDto

object UserRoutes : RouteRegistrar {
    override fun Route.register() {
        val userService by inject<UserService>()

        authenticate("auth-jwt") {
            get<ApiV1.Users> {
                call.requireRole(Role.ADMIN)
                call.respondSuccess(userService.getAll().map { it.toDto() })
            }

            get<ApiV1.Users.Id> { resource ->
                val principal = call.requireRole(Role.ADMIN, Role.DRIVER, Role.MEMBER)
                val requestedId = Uuid.parse(resource.id)
                val currentUserId = principal.getUserId()
                val role = principal.getRole()

                // Non-admin users can only view their own profile
                if (role != Role.ADMIN && requestedId != currentUserId) {
                    throw ApiException(HttpStatusCode.Forbidden, message = "You can only view your own profile")
                }

                val user = userService.getById(requestedId)
                    ?: throw ApiException(HttpStatusCode.NotFound, message = "User not found")
                call.respondSuccess(user.toDto())
            }

            delete<ApiV1.Users.Id> { resource ->
                call.requireRole(Role.ADMIN)
                val id = Uuid.parse(resource.id)
                if (userService.delete(id)) {
                    call.respondSuccess(Unit, HttpStatusCode.NoContent)
                } else {
                    call.respondError(HttpStatusCode.NotFound, "User not found")
                }
            }

            patch<ApiV1.Users.Id> { resource ->
                call.requireRole(Role.ADMIN)
                val id = Uuid.parse(resource.id)
                val patchRequest = call.receive<PatchUserRequestDto>()
                
                // Get existing user
                val existingUser = userService.getById(id)
                    ?: throw ApiException(HttpStatusCode.NotFound, message = "User not found")
                
                // Apply patches - only update fields that are provided
                val updatedUser = existingUser.copy(
                    name = patchRequest.name ?: existingUser.name,
                    email = patchRequest.email ?: existingUser.email,
                    role = patchRequest.role ?: existingUser.role
                )
                
                val result = userService.update(id, updatedUser)
                    ?: throw ApiException(HttpStatusCode.NotFound, message = "User not found")
                
                call.respondSuccess(result.toDto())
            }
        }
    }
}
