package dev.koenv.rentmycar.shared.network

import io.ktor.client.engine.*
import io.ktor.client.engine.okhttp.*

/**
 * Android implementation of HTTP engine factory using OkHttp.
 */
actual object HttpEngineFactory {
    actual fun createEngine(): HttpClientEngine = OkHttp.create()
}
