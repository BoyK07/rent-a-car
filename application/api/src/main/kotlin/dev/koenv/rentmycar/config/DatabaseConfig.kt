package dev.koenv.rentmycar.config

import io.ktor.server.application.*

data class PostgresConfig(
    val url: String,
    val user: String,
    val password: String,
    val embedded: Boolean
)

fun Application.postgresConfig(): PostgresConfig {
    val cfg = environment.config
    val embedded = cfg.propertyOrNull("postgres.embedded")
        ?.getString()?.toBooleanStrictOrNull() ?: true

    return if (embedded) {
        PostgresConfig(
            url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
            user = "root",
            password = "",
            embedded = true
        )
    } else {
        PostgresConfig(
            url = cfg.property("postgres.url").getString(),
            user = cfg.property("postgres.user").getString(),
            password = cfg.property("postgres.password").getString(),
            embedded = false
        )
    }
}
