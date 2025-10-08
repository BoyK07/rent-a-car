package dev.koenv.rentmycar.storage.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.koenv.rentmycar.config.mySqlConfig
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database

object DatabaseFactory {

    private fun createHikari(app: Application): HikariDataSource {
        val mysql = app.mySqlConfig()
        val hc = HikariConfig().apply {
            jdbcUrl = mysql.url
            username = mysql.user
            password = mysql.password
            driverClassName = if (mysql.embedded) "org.h2.Driver" else "com.mysql.cj.jdbc.Driver"
            maximumPoolSize = app.environment.config.propertyOrNull("mysql.pool.maxPoolSize")?.getString()?.toInt() ?: 8
            minimumIdle = app.environment.config.propertyOrNull("mysql.pool.minIdle")?.getString()?.toInt() ?: 0
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
