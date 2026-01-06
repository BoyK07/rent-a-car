package dev.koenv.rentmycar.shared.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.io.File

/**
 * JVM implementation of DatabaseDriverFactory.
 * Creates an SQLite driver using JDBC for JVM platforms.
 */
actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        // Create database in user home directory for JVM
        val databasePath = File(System.getProperty("user.home"), ".rentmycar/rentmycar.db")
        databasePath.parentFile?.mkdirs()
        
        val driver = JdbcSqliteDriver("jdbc:sqlite:${databasePath.absolutePath}")
        RentMyCarDatabase.Schema.create(driver)
        return driver
    }
}
