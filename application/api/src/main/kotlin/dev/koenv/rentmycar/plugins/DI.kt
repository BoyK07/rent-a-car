package dev.koenv.rentmycar.plugins

import dev.koenv.rentmycar.plugins.di.repositoryModule
import dev.koenv.rentmycar.plugins.di.serviceModule
import org.koin.dsl.module
import io.ktor.server.application.*
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import org.koin.core.logger.Level

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

                // repositories
                repositoryModule

                // services
                serviceModule
            }
        )
    }
}
