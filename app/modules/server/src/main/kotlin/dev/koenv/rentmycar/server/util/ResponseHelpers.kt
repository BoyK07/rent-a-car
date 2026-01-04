package dev.koenv.rentmycar.server.util

import dev.koenv.rentmycar.shared.http.ApiResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*

/**
 * Responds with a successful ApiResponse wrapper.
 * 
 * @param data The data to include in the response
 * @param status The HTTP status code (defaults to 200 OK)
 * @param message Optional message to include
 */
suspend inline fun <reified T : Any> ApplicationCall.respondSuccess(
    data: T,
    status: HttpStatusCode = HttpStatusCode.OK,
    message: String? = null
) {
    respond(status, ApiResponse(
        success = true,
        data = data,
        message = message,
        statusCode = status.value,
        error = null
    ))
}

/**
 * Responds with a successful ApiResponse wrapper for created resources.
 * 
 * @param data The created resource data
 * @param message Optional message to include
 */
suspend inline fun <reified T : Any> ApplicationCall.respondCreated(
    data: T,
    message: String? = null
) {
    respondSuccess(data, HttpStatusCode.Created, message)
}

/**
 * Responds with an error ApiResponse wrapper.
 * 
 * @param status The HTTP status code
 * @param message The error message
 * @param code The error code (defaults to uppercase status description)
 * @param traceId Optional trace ID for debugging
 * @param details Optional additional error details
 */
suspend fun ApplicationCall.respondError(
    status: HttpStatusCode,
    message: String,
    code: String? = null,
    traceId: String? = null,
    details: Map<String, String>? = null
) {
    respond(status, ApiResponse<Nothing?>(
        success = false,
        data = null,
        message = message,
        statusCode = status.value,
        error = dev.koenv.rentmycar.shared.http.ErrorDetails(
            code = code ?: status.description.uppercase().replace(Regex("[^A-Z0-9]+"), "_").trim('_'),
            traceId = traceId,
            details = details
        )
    ))
}
