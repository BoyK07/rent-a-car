package dev.koenv.rentmycar.shared.util

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import java.util.UUID

class RequestAborted : RuntimeException()

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