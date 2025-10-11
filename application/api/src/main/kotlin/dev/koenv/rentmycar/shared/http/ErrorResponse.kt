package dev.koenv.rentmycar.shared.http

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    val success: Boolean = false,
    val code: String,          // stable machine code, e.g. AUTH_INVALID_CREDENTIALS
    val message: String,       // human-readable summary
    val status: Int,           // HTTP status code
    val traceId: String? = null, // from CallId if present
    val details: Map<String, String>? = null // optional field-specific errors
)
