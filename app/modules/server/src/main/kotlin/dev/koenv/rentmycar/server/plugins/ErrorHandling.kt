package dev.koenv.rentmycar.server.plugins

import dev.koenv.rentmycar.server.util.RequestAborted
import dev.koenv.rentmycar.server.util.respondError
import dev.koenv.rentmycar.shared.http.ApiException
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*

/**
 * Global error handling for all uncaught exceptions.
 * Ensures all responses follow the unified ApiResponse format.
 */
fun Application.configureErrorHandling() {
    val log = environment.log

    install(StatusPages) {
        // Handle RequestAborted - client disconnected, don't need to respond
        exception<RequestAborted> { call, _ ->
            log.debug("RequestAborted: uri=${call.request.uri} trace=${call.callId}")
        }

        // Handle ApiException - business logic errors (used by non-converted routes and helper functions)
        exception<ApiException> { call, exception ->
            log.warn(
                "ApiException: status=${exception.http} code=${exception.code} " +
                "uri=${call.request.uri} trace=${call.callId} message=${exception.message}"
            )
            call.respondError(
                status = exception.http,
                message = exception.message ?: "An error occurred",
                code = exception.code,
                traceId = call.callId
            )
        }

        // Catch-all for any other uncaught exceptions
        exception<Throwable> { call, exception ->
            log.error("Unhandled exception: uri=${call.request.uri} trace=${call.callId}", exception)
            call.respondError(
                status = HttpStatusCode.InternalServerError,
                message = "Internal Server Error",
                code = "INTERNAL_SERVER_ERROR",
                traceId = call.callId
            )
        }

        // Handle 401 Unauthorized status
        status(HttpStatusCode.Unauthorized) { call, status ->
            call.respondError(
                status = status,
                message = "Unauthorized",
                code = "UNAUTHORIZED",
                traceId = call.callId
            )
        }

        // Handle 403 Forbidden status
        status(HttpStatusCode.Forbidden) { call, status ->
            call.respondError(
                status = status,
                message = "Forbidden",
                code = "FORBIDDEN",
                traceId = call.callId
            )
        }

        // Handle 404 Not Found status
        status(HttpStatusCode.NotFound) { call, status ->
            call.respondError(
                status = status,
                message = "Resource not found",
                code = "NOT_FOUND",
                traceId = call.callId
            )
        }

        // Handle 405 Method Not Allowed status
        status(HttpStatusCode.MethodNotAllowed) { call, status ->
            call.respondError(
                status = status,
                message = "Method not allowed",
                code = "METHOD_NOT_ALLOWED",
                traceId = call.callId
            )
        }
    }
}
