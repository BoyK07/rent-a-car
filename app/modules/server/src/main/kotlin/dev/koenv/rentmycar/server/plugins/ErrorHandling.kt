package dev.koenv.rentmycar.server.plugins

import dev.koenv.rentmycar.server.util.RequestAborted
import dev.koenv.rentmycar.shared.http.ApiException
import dev.koenv.rentmycar.shared.http.ErrorResponse
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.exceptions.ExposedSQLException
import org.slf4j.Logger

fun Application.configureErrorHandling() {
    val log = environment.log
    val json = Json { encodeDefaults = true }

    install(StatusPages) {
        exception<ApiException> { call, exception ->
            handleApiException(call, exception, log)
        }

        exception<RequestAborted> { call, _ ->
            handleRequestAborted(call, log)
        }

        exception<BadRequestException> { call, exception ->
            handleBadRequest(call, exception, log)
        }

        exception<SerializationException> { call, exception ->
            handleSerializationException(call, exception, log)
        }

        exception<ExposedSQLException> { call, exception ->
            handleDatabaseException(call, exception, log)
        }

        exception<Throwable> { call, exception ->
            handleUnknownException(call, exception, log)
        }

        status(HttpStatusCode.NotFound) { call, status ->
            call.respond(status)
        }

        status(HttpStatusCode.MethodNotAllowed) { call, status ->
            call.respond(status)
        }
    }

    // Wrap plain status sends into a JSON body and BYPASS content negotiation
    sendPipeline.intercept(ApplicationSendPipeline.Transform) { subject ->
        if (subject is HttpStatusCode) {
            val errorResponse = ErrorResponse(
                code = subject.description.uppercase().replace(Regex("[^A-Z0-9]+"), "_").trim('_'),
                message = subject.description,
                status = subject.value,
                traceId = call.callId
            )
            val payload = json.encodeToString(ErrorResponse.serializer(), errorResponse)
            log.debug("Wrapping status ${subject.value} into JSON: uri=${call.request.uri} trace=${call.callId}")
            call.response.status(subject)
            // Force JSON regardless of Accept header to avoid 406
            proceedWith(TextContent(payload, ContentType.Application.Json, subject))
        }
    }
}

/**
 * Handles business logic exceptions thrown via ApiException
 */
private suspend fun handleApiException(call: ApplicationCall, exception: ApiException, log: Logger) {
    log.warn(
        "ApiException: status=${exception.http.value} code=${exception.code} " +
                "uri=${call.request.uri} trace=${call.callId} message=${exception.message}"
    )
    call.respond(exception.http, exception.toErrorResponse(call))
}

/**
 * Handles aborted requests (client disconnected, timeout, etc.)
 */
private fun handleRequestAborted(call: ApplicationCall, log: Logger) {
    log.debug("RequestAborted: uri=${call.request.uri} trace=${call.callId}")
}

/**
 * Handles invalid request body or missing required fields
 */
private suspend fun handleBadRequest(call: ApplicationCall, exception: BadRequestException, log: Logger) {
    log.info("BadRequest: uri=${call.request.uri} trace=${call.callId} message=${exception.message}")
    call.respond(
        HttpStatusCode.BadRequest,
        ErrorResponse(
            code = "INVALID_REQUEST_BODY",
            message = "Request body is missing required fields or has invalid format",
            status = HttpStatusCode.BadRequest.value,
            traceId = call.callId
        )
    )
}

/**
 * Handles JSON serialization/deserialization errors
 */
private suspend fun handleSerializationException(
    call: ApplicationCall,
    exception: SerializationException,
    log: Logger
) {
    log.warn("SerializationException: uri=${call.request.uri} trace=${call.callId}", exception)
    call.respond(
        HttpStatusCode.BadRequest,
        ErrorResponse(
            code = "INVALID_JSON",
            message = exception.message ?: "Invalid JSON structure",
            status = HttpStatusCode.BadRequest.value,
            traceId = call.callId
        )
    )
}

/**
 * Handles database constraint violations (foreign key, unique, etc.)
 */
private suspend fun handleDatabaseException(call: ApplicationCall, exception: ExposedSQLException, log: Logger) {
    val errorMessage = exception.cause?.message ?: exception.message ?: "Database error"

    // Check for foreign key constraint violations
    if (errorMessage.contains("Cannot add or update a child row", ignoreCase = true) ||
        errorMessage.contains("FOREIGN KEY", ignoreCase = true)
    ) {
        log.warn("FK constraint violation: uri=${call.request.uri} trace=${call.callId}")
        call.respond(
            HttpStatusCode.BadRequest,
            ErrorResponse(
                code = "FOREIGN_KEY_VIOLATION",
                message = "Referenced entity does not exist",
                status = HttpStatusCode.BadRequest.value,
                traceId = call.callId
            )
        )
        return
    }

    // Check for unique constraint violations
    if (errorMessage.contains("Duplicate entry", ignoreCase = true) ||
        errorMessage.contains("UNIQUE", ignoreCase = true)
    ) {
        log.warn("Unique constraint violation: uri=${call.request.uri} trace=${call.callId}")
        call.respond(
            HttpStatusCode.Conflict,
            ErrorResponse(
                code = "DUPLICATE_ENTRY",
                message = "A record with this value already exists",
                status = HttpStatusCode.Conflict.value,
                traceId = call.callId
            )
        )
        return
    }

    // Unknown database error
    log.error("Database error: uri=${call.request.uri} trace=${call.callId}", exception)
    call.respond(
        HttpStatusCode.InternalServerError,
        ErrorResponse(
            code = "DATABASE_ERROR",
            message = "Database constraint violation",
            status = HttpStatusCode.InternalServerError.value,
            traceId = call.callId
        )
    )
}

/**
 * Handles all unexpected exceptions
 */
private suspend fun handleUnknownException(call: ApplicationCall, exception: Throwable, log: Logger) {
    log.error("Unhandled exception: uri=${call.request.uri} trace=${call.callId}", exception)
    call.respond(
        HttpStatusCode.InternalServerError,
        ErrorResponse(
            code = "INTERNAL_SERVER_ERROR",
            message = "Internal Server Error",
            status = HttpStatusCode.InternalServerError.value,
            traceId = call.callId
        )
    )
}


/**
 * Extension function to convert ApiException to ErrorResponse
 */
private fun ApiException.toErrorResponse(call: ApplicationCall) = ErrorResponse(
    code = code,
    message = message,
    status = http.value,
    traceId = call.callId
)
