package dev.koenv.rentmycar

import dev.koenv.rentmycar.plugins.*
import io.ktor.server.application.*
import io.ktor.server.netty.EngineMain

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    configureHTTP()
    configureSerialization()
    configureDatabase()
    configureMonitoring()
    configureAdministration()
    configureSecurity()
    configureErrorHandling()
    configureRouting()
}
