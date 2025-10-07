package dev.koenv.rentmycar.storage.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.koenv.rentmycar.config.postgresConfig
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database

object DatabaseFactory {

    private fun createHikari(app: Application): HikariDataSource {
        val pg = app.postgresConfig()
        val hc = HikariConfig().apply {
            jdbcUrl = pg.url
            username = pg.user
            password = pg.password
            driverClassName = if (pg.embedded) "org.h2.Driver" else "org.postgresql.Driver"
            maximumPoolSize = app.environment.config.propertyOrNull("postgres.pool.maxPoolSize")?.getString()?.toInt() ?: 8
            minimumIdle = app.environment.config.propertyOrNull("postgres.pool.minIdle")?.getString()?.toInt() ?: 0
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }
        return HikariDataSource(hc)
    }

    fun connect(app: Application): Database {
        val ds = createHikari(app)
        app.log.info("Connecting DB via Hikari (${ds.jdbcUrl})")
        return Database.connect(ds)
    }
}
