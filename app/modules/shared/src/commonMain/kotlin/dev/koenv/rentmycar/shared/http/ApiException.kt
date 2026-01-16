package dev.koenv.rentmycar.shared.http

import io.ktor.http.*

/**
 * Custom exception for API errors with structured error information.
 * 
 * Provides:
 * - HTTP status code for error categorization
 * - Machine-readable error code (auto-generated or custom)
 * - Human-readable error message
 * - Optional additional details
 * 
 * Used throughout the application for consistent error handling.
 */
class ApiException(
    val http: HttpStatusCode,
    code: String? = null,
    override val message: String,
    val details: Any? = null
) : RuntimeException(message) {
    val code: String = code ?: http.description.uppercase().replace(Regex("[^A-Z0-9]+"), "_").trim('_')
}
