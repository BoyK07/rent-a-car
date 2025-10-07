package dev.koenv.rentmycar

import dev.koenv.rentmycar.domain.services.configureAdministration
import dev.koenv.rentmycar.plugins.configureDatabase
import dev.koenv.rentmycar.plugins.configureHTTP
import dev.koenv.rentmycar.plugins.configureMonitoring
import dev.koenv.rentmycar.plugins.configureRouting
import dev.koenv.rentmycar.plugins.configureSecurity
import dev.koenv.rentmycar.plugins.configureSerialization
import dev.koenv.rentmycar.plugins.configureServices
import io.ktor.server.application.*
import io.ktor.server.netty.EngineMain

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    configureHTTP()
    configureSerialization()
    configureDatabase()
    configureServices()
    configureMonitoring()
    configureAdministration()
    configureSecurity()
    configureRouting()
}
