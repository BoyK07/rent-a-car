package dev.koenv.rentmycar.shared.db

import app.cash.sqldelight.db.SqlDriver

/**
 * Factory for creating platform-specific SQL drivers.
 * Implementations provided per platform (Android/JVM).
 */
expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}
