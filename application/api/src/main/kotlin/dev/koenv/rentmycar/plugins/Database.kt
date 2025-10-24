package dev.koenv.rentmycar.plugins

import ch.vorburger.mariadb4j.DB
import ch.vorburger.mariadb4j.DBConfigurationBuilder
import io.ktor.server.application.*
import io.ktor.server.config.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.io.File

private var embeddedDb: DB? = null

fun Application.configureDatabase() {
    initDatabase(environment.config)

    // Listen for shutdown
    monitor.subscribe(ApplicationStopped) {
        stopEmbeddedDatabase()
    }

}

private fun initDatabase(config: ApplicationConfig) {
    val dbConfig = config.config("db")
    val type = dbConfig.property("type").getString().lowercase()

    val (url, driver, user, password) = when (type) {
        "external" -> {
            val host = dbConfig.property("host").getString()
            val port = dbConfig.property("port").getString()
            val name = dbConfig.property("name").getString()
            val user = dbConfig.property("user").getString()
            val password = dbConfig.property("password").getString()
            listOf(
                "jdbc:mariadb://$host:$port/$name?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
                "org.mariadb.jdbc.Driver",
                user,
                password
            )
        }
        "embedded" -> {
            val baseDir = File("build/mariadb4j")
            val dbName = dbConfig.propertyOrNull("name")?.getString() ?: "rentmycar"

            val configBuilder = DBConfigurationBuilder.newBuilder()
            configBuilder.setPort(3316) // consider 0 to auto-pick a free port in dev
            configBuilder.setBaseDir(baseDir)
            configBuilder.setDataDir(File(baseDir, "data"))
            configBuilder.setDeletingTemporaryBaseAndDataDirsOnShutdown(false)

            val db = DB.newEmbeddedDB(configBuilder.build())
            db.start()
            embeddedDb = db

            val port = configBuilder.port
            val url = "jdbc:mariadb://localhost:$port/$dbName?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
            db.createDB(dbName)

            listOf(url, "org.mariadb.jdbc.Driver", "root", "")
        }
        else -> error("Unsupported database type: $type")
    }

    // Exposed connect
    Database.connect(url, driver, user, password)

    // Flags (env or application.conf). Only use in dev!
    val reset = System.getenv("DB_RESET")?.equals("true", ignoreCase = true)
        ?: config.propertyOrNull("db.reset")?.getString()?.equals("true", true)
        ?: false

    val flyway = Flyway.configure()
        .dataSource(url, user, password)
        .locations("classpath:migrations")   // keep your folder
        .cleanDisabled(false)                // allow clean (careful: dev only)
        .load()

    when {
        reset -> {
            // migrate:fresh
            flyway.clean()
            flyway.migrate()
        }
        else -> {
            flyway.migrate()
        }
    }
}

/** Gracefully stop MariaDB4j on shutdown. */
fun stopEmbeddedDatabase() {
    embeddedDb?.stop()
}

/** Run Exposed transactions on IO dispatcher. */
suspend fun <T> dbQuery(block: () -> T): T =
    withContext(Dispatchers.IO) {
        transaction { block() }
    }
