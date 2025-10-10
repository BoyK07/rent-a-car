package dev.koenv.rentmycar.plugins

import dev.koenv.rentmycar.storage.db.DatabaseFactory
import io.ktor.server.application.*

fun Application.configureDatabase() {
    DatabaseFactory.init(environment.config)
}
