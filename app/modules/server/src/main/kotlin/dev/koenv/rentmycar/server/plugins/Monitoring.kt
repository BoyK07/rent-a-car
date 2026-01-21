package dev.koenv.rentmycar.server.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import kotlin.uuid.Uuid

/**
 * Configures monitoring and observability for the application.
 * 
 * Features:
 * - **Prometheus Metrics**: Collects application metrics (requests, latency, errors)
 *   exposed at `/metrics-micrometer` endpoint
 * - **Call ID**: Generates unique request IDs for tracing
 *   - Reads from X-Request-Id header if present
 *   - Generates UUID if not provided
 *   - Echoes back in response header
 * - **Call Logging**: Logs all requests with call ID for debugging
 * 
 * Useful for production monitoring, debugging, and performance analysis.
 */
fun Application.configureMonitoring() {
    val appMicrometerRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

    install(MicrometerMetrics) { registry = appMicrometerRegistry }

    install(CallId) {
        header(HttpHeaders.XRequestId)
        generate { Uuid.random().toString() }
        replyToHeader(HttpHeaders.XRequestId)
        verify { it.length in 8..128 }
    }

    install(CallLogging) { callIdMdc("call-id") }

    routing {
        get("/metrics-micrometer") { call.respond(appMicrometerRegistry.scrape()) }
    }
}
