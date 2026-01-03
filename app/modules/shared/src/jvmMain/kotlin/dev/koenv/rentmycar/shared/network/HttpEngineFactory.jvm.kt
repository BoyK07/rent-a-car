package dev.koenv.rentmycar.shared.network

import io.ktor.client.engine.*
import io.ktor.client.engine.cio.*

/**
 * JVM/Desktop implementation of HTTP engine factory using CIO.
 */
actual object HttpEngineFactory {
    actual fun createEngine(): HttpClientEngine = CIO.create()
}
