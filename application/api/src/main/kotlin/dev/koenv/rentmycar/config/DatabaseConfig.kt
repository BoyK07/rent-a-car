package dev.koenv.rentmycar.config

import io.ktor.server.application.*

data class MySqlConfig(
    val url: String,
    val user: String,
    val password: String,
    val embedded: Boolean
)

fun Application.mySqlConfig(): MySqlConfig {
    val cfg = environment.config
    val embedded = cfg.propertyOrNull("mysql.embedded")
        ?.getString()?.toBooleanStrictOrNull() ?: true

    return if (embedded) {
        MySqlConfig(
            url = "jdbc:h2:mem:test;MODE=MySQL;DB_CLOSE_DELAY=-1",
            user = "sa",
            password = "",
            embedded = true
        )
    } else {
        MySqlConfig(
            url = cfg.property("mysql.url").getString(),
            user = cfg.property("mysql.user").getString(),
            password = cfg.property("mysql.password").getString(),
            embedded = false
        )
    }
}
