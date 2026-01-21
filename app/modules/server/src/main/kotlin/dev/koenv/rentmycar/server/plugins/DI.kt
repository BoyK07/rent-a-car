package dev.koenv.rentmycar.server.plugins

import dev.koenv.rentmycar.server.plugins.di.repositoryModule
import dev.koenv.rentmycar.server.plugins.di.serviceModule
import io.ktor.server.application.*
import org.koin.core.logger.Level
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

/**
 * Configures dependency injection using Koin.
 * 
 * Modules registered:
 * - Application config (Ktor environment configuration)
 * - Repository module (data access layer)
 * - Service module (business logic layer)
 * 
 * Koin logging level can be controlled via KOIN_LOG_LEVEL environment variable:
 * - DEBUG: Verbose logging for development
 * - INFO: Standard logging (default)
 * - WARN/WARNING: Only warnings
 * - ERROR: Only errors
 * - NONE/OFF: No logging
 */
fun Application.configureDI() {
    install(Koin) {
        slf4jLogger(
            (System.getenv("KOIN_LOG_LEVEL") ?: "INFO").uppercase().let {
                when (it) {
                    "DEBUG" -> Level.DEBUG
                    "INFO" -> Level.INFO
                    "WARN", "WARNING" -> Level.WARNING
                    "ERROR" -> Level.ERROR
                    "NONE", "OFF" -> Level.NONE
                    else -> Level.INFO
                }
            }
        )

        modules(
            module {
                // Provide Ktor config for services that need it
                single { environment.config }
            },

            // Data access layer
            repositoryModule,

            // Business logic layer
            serviceModule
        )
    }
}
