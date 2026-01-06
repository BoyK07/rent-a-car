package dev.koenv.rentmycar.shared.db

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

/**
 * Android implementation of DatabaseDriverFactory.
 * Creates an SQLite driver using the Android SQLite implementation.
 */
actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(
            schema = RentMyCarDatabase.Schema,
            context = context,
            name = "rentmycar.db"
        )
    }
}
