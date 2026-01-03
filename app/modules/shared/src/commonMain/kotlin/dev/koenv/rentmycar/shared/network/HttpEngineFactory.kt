package dev.koenv.rentmycar.shared.network

import io.ktor.client.engine.*

/**
 * Factory to provide platform-specific HTTP engine for Ktor client.
 */
expect object HttpEngineFactory {
    fun createEngine(): HttpClientEngine
}
