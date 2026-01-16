package dev.koenv.rentmycar.server.plugins

import io.github.flaxoos.ktor.server.plugins.ratelimiter.RateLimiting
import io.github.flaxoos.ktor.server.plugins.ratelimiter.implementations.TokenBucket
import io.ktor.server.application.*
import io.ktor.server.routing.*
import kotlin.time.Duration.Companion.seconds

/**
 * Configures rate limiting for the application.
 * 
 * Uses Token Bucket algorithm to prevent API abuse:
 * - Capacity: 100 requests per bucket
 * - Refill rate: 10 seconds per token
 * 
 * This allows bursts of up to 100 requests, with sustained rate
 * of ~6 requests per minute per client.
 * 
 * Applied to all routes under the root path.
 */
fun Application.configureAdministration() {
    routing {
        route("/") {
            install(RateLimiting) {
                rateLimiter {
                    type = TokenBucket::class
                    capacity = 100
                    rate = 10.seconds
                }
            }
        }
    }
}
