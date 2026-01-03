package dev.koenv.rentmycar.server.plugins

import dev.koenv.rentmycar.server.plugins.di.repositoryModule
import dev.koenv.rentmycar.server.plugins.di.serviceModule
import io.ktor.server.application.*
import org.koin.core.logger.Level
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

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
                // provide Ktor config
                single { environment.config }
            },

            // repositories
            repositoryModule,

            // services
            serviceModule
        )
    }
}
