package dev.koenv.rentmycar.shared.http

import io.ktor.http.*

/**
 * Represents an API-level exception with an HTTP status,
 * a human-readable message, and an optional machine-readable code.
 */
class ApiException(
    val http: HttpStatusCode,
    code: String? = null,
    override val message: String,
    val details: Any? = null
) : RuntimeException(message) {
    val code: String = code ?: http.description.uppercase().replace(Regex("[^A-Z0-9]+"), "_").trim('_')
}
