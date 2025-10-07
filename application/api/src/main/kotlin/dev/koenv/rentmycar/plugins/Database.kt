package dev.koenv.rentmycar.plugins

import dev.koenv.rentmycar.storage.db.DatabaseFactory
import io.ktor.server.application.*
import io.ktor.util.AttributeKey
import org.jetbrains.exposed.sql.Database

private val DbKey = AttributeKey<Database>("exposed-db")

fun Application.configureDatabase() {
    if (!attributes.contains(DbKey)) {
        val db = DatabaseFactory.connect(this)
        attributes.put(DbKey, db)
        log.info("Database connected successfully (no migrations run).")
    }
}

fun Application.database(): Database = attributes[DbKey]
