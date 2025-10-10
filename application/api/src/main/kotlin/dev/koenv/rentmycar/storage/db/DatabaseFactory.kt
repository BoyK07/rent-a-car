package dev.koenv.rentmycar.storage.db

import org.flywaydb.core.Flyway
import io.ktor.server.config.ApplicationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

object DatabaseFactory {
    fun init(config: ApplicationConfig) {
        val dbConfig = config.config("db")
        val type = dbConfig.property("type").getString().lowercase()

        val (url, driver, user, password) = when (type) {
            "mysql" -> {
                val host = dbConfig.property("host").getString()
                val port = dbConfig.property("port").getString()
                val name = dbConfig.property("name").getString()
                val user = dbConfig.property("user").getString()
                val password = dbConfig.property("password").getString()
                listOf(
                    "jdbc:mysql://$host:$port/$name?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
                    "com.mysql.cj.jdbc.Driver",
                    user,
                    password
                )
            }
            "sqlite" -> {
                val file = dbConfig.property("file").getString()
                listOf("jdbc:sqlite:$file", "org.sqlite.JDBC", "", "")
            }
            else -> {
                listOf("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;", "org.h2.Driver", "", "")
            }
        }

        Database.connect(url, driver, user, password)

        // Run Flyway migrations
        Flyway.configure()
            .dataSource(url, user, password)
            .locations("classpath:migrations")
            .load()
            .migrate()
    }

    suspend fun <T> dbQuery(block: () -> T): T =
        withContext(Dispatchers.IO) {
            transaction { block() }
        }

}
