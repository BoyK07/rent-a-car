package dev.koenv.rentmycar.server.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import kotlin.uuid.Uuid

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
