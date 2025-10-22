package dev.koenv.rentmycar.plugins

import dev.koenv.rentmycar.shared.http.ApiException
import dev.koenv.rentmycar.shared.http.ErrorResponse
import dev.koenv.rentmycar.shared.util.RequestAborted
import io.ktor.http.*
import io.ktor.http.content.TextContent
import io.ktor.server.application.*
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.callid.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

fun Application.configureErrorHandling() {
    val log = environment.log
    val json = Json { encodeDefaults = true }

    install(StatusPages) {
        exception<ApiException> { call, e ->
            log.warn("ApiException ${e.http.value} ${e.code} uri=${call.request.uri} trace=${call.callId}: ${e.message}")
            call.respond(e.http, e.toErrorResponse(call))
        }
        exception<RequestAborted> { call, _ ->
            log.debug("RequestAborted uri=${call.request.uri} trace=${call.callId}")
        }

        exception<BadRequestException> { call, _ ->
            log.info("Handled BadRequest uri=${call.request.uri} trace=${call.callId}")
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

        exception<SerializationException> { call, e ->
            log.warn("BadRequest SerializationException uri=${call.request.uri} trace=${call.callId}", e)
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(
                    code = "INVALID_JSON",
                    message = e.message ?: "Invalid JSON structure",
                    status = HttpStatusCode.BadRequest.value,
                    traceId = call.callId
                )
            )
        }

        exception<Throwable> { call, e ->
            log.error("Unhandled 500 uri=${call.request.uri} trace=${call.callId}", e)
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse(
                    code = "INTERNAL_SERVER_ERROR",
                    message = "Internal Server Error",
                    status = 500,
                    traceId = call.callId
                )
            )
        }

        status(HttpStatusCode.NotFound) { call, s -> call.respond(s) }
        status(HttpStatusCode.MethodNotAllowed) { call, s -> call.respond(s) }
    }


    // Wrap plain status sends into a JSON body and BYPASS content negotiation.
    sendPipeline.intercept(ApplicationSendPipeline.Transform) { subject ->
        if (subject is HttpStatusCode) {
            val s = subject
            val body = ErrorResponse(
                code = s.description.uppercase().replace(Regex("[^A-Z0-9]+"), "_").trim('_'),
                message = s.description,
                status = s.value,
                traceId = call.callId
            )
            val payload = json.encodeToString(ErrorResponse.serializer(), body)
            log.debug("Wrapping ${s.value} for uri=${call.request.uri} trace=${call.callId}")
            call.response.status(s)
            // Force JSON regardless of Accept header to avoid 406
            proceedWith(TextContent(payload, ContentType.Application.Json, s))
        }
    }
}

private fun ApiException.toErrorResponse(call: ApplicationCall) = ErrorResponse(
    code = code,
    message = message,
    status = http.value,
    traceId = call.callId
)
