package dev.koenv.rentmycar.shared.util

import dev.koenv.rentmycar.domain.enums.Role
import dev.koenv.rentmycar.shared.http.ApiException
import io.ktor.http.*
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.*
import java.util.UUID
import io.ktor.server.application.ApplicationCall

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

suspend fun ApplicationCall.requireUuidParamOrFail(name: String): UUID {
    val s = parameters[name] ?: run {
        throw ApiException(HttpStatusCode.BadRequest, message = "Missing required parameter '$name'")
    }
    return try {
        UUID.fromString(s)
    } catch (_: IllegalArgumentException) {
        throw ApiException(HttpStatusCode.BadRequest, message = "Invalid UUID format")
    }
}

suspend inline fun <reified T : Any> ApplicationCall.requireBodyOrFail(): T {
    return try {
        receive<T>()
    } catch (_: Exception) {
        throw ApiException(HttpStatusCode.BadRequest, message = "Invalid request body")
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

suspend fun ApplicationCall.requireBigDecimalParamOrNull(name: String): java.math.BigDecimal? {
    val s = parameters[name] ?: return null
    return try {
        java.math.BigDecimal(s)
    } catch (_: NumberFormatException) {
        throw ApiException(HttpStatusCode.BadRequest, message = "Invalid $name: must be a valid number")
    }
}

suspend fun ApplicationCall.requireLocalDateTimeParamOrNull(name: String): kotlinx.datetime.LocalDateTime? {
    val s = request.queryParameters[name] ?: return null
    return try {
        kotlinx.datetime.LocalDateTime.parse(s)
    } catch (_: IllegalArgumentException) {
        throw ApiException(HttpStatusCode.BadRequest, message = "Invalid $name: must be a valid ISO 8601 datetime")
    }
}