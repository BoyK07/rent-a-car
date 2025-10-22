package dev.koenv.rentmycar.shared.util

import dev.koenv.rentmycar.domain.enums.Role
import dev.koenv.rentmycar.shared.http.ApiException
import io.ktor.http.*
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.*
import io.ktor.server.response.*
import java.util.UUID
import io.ktor.server.application.ApplicationCall

class RequestAborted : RuntimeException()

fun JWTPrincipal.requireRole(vararg allowed: Role) {
    val role = this.payload.getClaim("role").asString()?.let { Role.valueOf(it) }
    if (role == null || role !in allowed) throw ApiException(HttpStatusCode.Forbidden, message = "Insufficient role")
}

fun ApplicationCall.requireRole(vararg allowed: Role): JWTPrincipal {
    val principal = this.principal<JWTPrincipal>() ?: throw ApiException(
        HttpStatusCode.InternalServerError,
        message = "No principal"
    )
    principal.requireRole(*allowed)
    return principal
}

suspend fun ApplicationCall.requireUuidParamOrFail(name: String): UUID {
    val s = parameters[name] ?: run {
        respond(HttpStatusCode.BadRequest, "Missing $name")
        throw RequestAborted()
    }
    return try {
        UUID.fromString(s)
    } catch (e: IllegalArgumentException) {
        respond(HttpStatusCode.BadRequest, "Invalid $name")
        throw RequestAborted()
    }
}

suspend inline fun <reified T : Any> ApplicationCall.requireBodyOrFail(): T {
    return try {
        receive<T>()
    } catch (e: Exception) {
        respond(HttpStatusCode.BadRequest, "Invalid request body")
        throw RequestAborted()
    }
}