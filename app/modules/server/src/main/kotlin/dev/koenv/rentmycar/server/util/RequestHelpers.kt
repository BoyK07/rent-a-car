package dev.koenv.rentmycar.server.util

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import dev.koenv.rentmycar.shared.domain.enums.Role
import dev.koenv.rentmycar.shared.http.ApiException
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import kotlinx.datetime.LocalDateTime
import kotlin.uuid.Uuid

class RequestAborted : RuntimeException()

/**
 * Ensures that the current JWT principal has one of the allowed roles,
 * taking role inheritance into account.
 */
fun JWTPrincipal.requireRole(vararg allowed: Role) {
    val role = this.payload.getClaim("role").asString()?.let { Role.valueOf(it) }
        ?: throw ApiException(HttpStatusCode.Forbidden, message = "Missing role claim")

    // Inheritance check: allowed if any allowed role is included by current role
    val permitted = allowed.any { role.includes(it) }

    if (!permitted) {
        throw ApiException(HttpStatusCode.Forbidden, message = "Insufficient role")
    }
}

/**
 * Retrieves and validates the authenticated principal,
 * enforcing role-based access with inheritance.
 */
fun ApplicationCall.requireRole(vararg allowed: Role): JWTPrincipal {
    val principal = this.principal<JWTPrincipal>()
        ?: throw ApiException(HttpStatusCode.InternalServerError, message = "No principal")
    principal.requireRole(*allowed)
    return principal
}

suspend fun ApplicationCall.requireUuidParamOrFail(name: String): Uuid {
    val s = parameters[name] ?: run {
        throw ApiException(HttpStatusCode.BadRequest, message = "Missing required parameter '$name'")
    }
    return try {
        Uuid.parse(s)
    } catch (_: IllegalArgumentException) {
        throw ApiException(HttpStatusCode.BadRequest, message = "Invalid Uuid format")
    }
}

/**
 * Parses an optional Uuid query parameter. Returns null when missing, 400 when invalid.
 */
suspend fun ApplicationCall.requireUuidParamOrNull(name: String): Uuid? {
    val s = parameters[name] ?: return null
    return try {
        Uuid.parse(s)
    } catch (_: IllegalArgumentException) {
        throw ApiException(HttpStatusCode.BadRequest, message = "Invalid $name: must be a valid Uuid")
    }
}

suspend inline fun <reified T : Any> ApplicationCall.requireBodyOrFail(): T {
    return try {
        receive<T>()
    } catch (_: Exception) {
        throw ApiException(HttpStatusCode.BadRequest, message = "Invalid request body")
    }
}

suspend fun ApplicationCall.requireBigDecimalParamOrNull(name: String): BigDecimal? {
    val s = parameters[name] ?: return null
    return try {
        BigDecimal.parseString(s)
    } catch (_: Exception) {
        throw ApiException(HttpStatusCode.BadRequest, message = "Invalid $name: must be a valid decimal number")
    }
}

suspend fun ApplicationCall.requireDoubleParamOrNull(name: String): Double? {
    val s = parameters[name] ?: return null
    return try {
        s.toDouble()
    } catch (_: NumberFormatException) {
        throw ApiException(HttpStatusCode.BadRequest, message = "Invalid $name: must be a valid number")
    }
}

suspend fun ApplicationCall.requireIntParamOrNull(name: String): Int? {
    val s = parameters[name] ?: return null
    return try {
        s.toInt()
    } catch (_: NumberFormatException) {
        throw ApiException(HttpStatusCode.BadRequest, message = "Invalid $name: must be a valid number")
    }
}

suspend fun ApplicationCall.requireStringParamOrNull(name: String): String? {
    return parameters[name]
}

suspend fun ApplicationCall.requireLocalDateTimeParamOrNull(name: String): LocalDateTime? {
    val s = request.queryParameters[name] ?: return null
    return try {
        LocalDateTime.parse(s)
    } catch (_: IllegalArgumentException) {
        throw ApiException(HttpStatusCode.BadRequest, message = "Invalid $name: must be a valid ISO 8601 datetime")
    }
}

/**
 * Parses an optional Long query parameter. Returns null when missing, 400 when invalid.
 */
suspend fun ApplicationCall.requireLongParamOrNull(name: String): Long? {
    val s = parameters[name] ?: return null
    return try {
        s.toLong()
    } catch (_: NumberFormatException) {
        throw ApiException(HttpStatusCode.BadRequest, message = "Invalid $name: must be a valid number")
    }
}

/**
 * Extracts the user ID from the JWT principal.
 */
fun JWTPrincipal.getUserId(): Uuid {
    return Uuid.parse(this.payload.getClaim("userId").asString())
}

/**
 * Extracts the role from the JWT principal.
 */
fun JWTPrincipal.getRole(): Role {
    return Role.valueOf(this.payload.getClaim("role").asString())
}

/**
 * Verifies that the current user is either an admin or the owner of the resource.
 * Throws ApiException with Forbidden status if the check fails.
 *
 * @param role The role of the current user
 * @param userId The ID of the current user
 * @param ownerId The ID of the resource owner
 * @param resourceName The name of the resource for the error message (e.g., "car", "photo")
 */
fun verifyOwnership(role: Role, userId: Uuid, ownerId: Uuid, resourceName: String = "resource") {
    if (role != Role.ADMIN && ownerId != userId) {
        throw ApiException(HttpStatusCode.Forbidden, message = "You are not the owner of this $resourceName")
    }
}
