package dev.koenv.rentmycar.server.plugins

import ch.vorburger.mariadb4j.DB
import ch.vorburger.mariadb4j.DBConfigurationBuilder
import dev.koenv.rentmycar.server.storage.db.seedData
import io.ktor.server.application.*
import io.ktor.server.config.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.io.File
import java.net.Socket

private var embeddedDb: DB? = null

/**
 * Supported database providers for the application.
 */
enum class DatabaseProvider {
    MARIADB_EXTERNAL,
    MARIADB_EMBEDDED,
    H2_MEMORY,
    H2_FILE;

    companion object {
        fun fromString(value: String): DatabaseProvider {
            return when (value.lowercase()) {
                "mariadb", "external" -> MARIADB_EXTERNAL
                "mariadb-embedded", "embedded" -> MARIADB_EMBEDDED
                "h2", "h2-file" -> H2_FILE
                "h2-memory", "h2-mem" -> H2_MEMORY
                else -> throw IllegalArgumentException(
                    "Unsupported database provider: $value. " +
                    "Supported values: mariadb, mariadb-embedded, h2, h2-memory, h2-file"
                )
            }
        }
    }
}

/**
 * Configures database connection and migrations.
 * 
 * Supports multiple database providers:
 * - **mariadb / external**: Connect to existing MariaDB instance (production/staging)
 * - **mariadb-embedded / embedded**: Start MariaDB4j embedded instance (legacy, has process lifecycle issues)
 * - **h2 / h2-memory**: H2 in-memory database (default, fast development/testing)
 * - **h2-file**: H2 file-based database (development with data persistence)
 * 
 * Uses Flyway for schema migrations from classpath:migrations.
 * 
 * Optional DB_RESET environment variable or db.reset config will:
 * - Clean the database (DROP all tables)
 * - Re-run all migrations from scratch
 * - WARNING: Only use in development!
 */
fun Application.configureDatabase() {
    initDatabase(environment.config)

    monitor.subscribe(ApplicationStopped) {
        stopEmbeddedDatabase()
    }
    
    // Seed data in development mode if enabled
    val seedData = System.getenv("DB_SEED")?.equals("true", ignoreCase = true)
        ?: environment.config.propertyOrNull("db.seed")?.getString()?.equals("true", ignoreCase = true)
        ?: false
    
    if (seedData) {
        launch {
            try {
                seedData()
            } catch (e: Exception) {
                log.error("Failed to seed data: ${e.message}", e)
            }
        }
    }
}

private fun initDatabase(config: ApplicationConfig) {
    val dbConfig = config.config("db")
    val providerString = dbConfig.property("provider").getString()
    val provider = DatabaseProvider.fromString(providerString)

    val (url, driver, user, password) = when (provider) {
        DatabaseProvider.MARIADB_EXTERNAL -> {
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

        DatabaseProvider.MARIADB_EMBEDDED -> {
            val baseDir = File("build/mariadb4j")
            val dbName = dbConfig.propertyOrNull("name")?.getString() ?: "rentmycar"
            val port = 3306

            if (isPortInUse(port)) {
                throw RuntimeException(
                    "ERROR: Port $port is already in use!\n" +
                    "\n" +
                    "The embedded database cannot start because port $port is occupied.\n" +
                    "This could be:\n" +
                    "  • An orphaned MariaDB process from a previous run\n" +
                    "  • Another MariaDB/MySQL instance\n" +
                    "  • Any other application using port $port\n" +
                    "\n" +
                    "To resolve this:\n" +
                    "  1. Find and stop the process using port $port\n" +
                    "     Windows: tasklist | findstr :$port  (then: taskkill /F /PID <pid>)\n" +
                    "     Linux/Mac: lsof -i :$port  (then: kill -9 <pid>)\n" +
                    "  2. Or change the database port in application.yaml\n" +
                    "  3. Or use a different database provider (e.g., h2)\n"
                )
            }

            val configBuilder = DBConfigurationBuilder.newBuilder()
            configBuilder.setPort(port)
            configBuilder.setBaseDir(baseDir)
            configBuilder.setDataDir(File(baseDir, "data"))
            configBuilder.setDeletingTemporaryBaseAndDataDirsOnShutdown(false)

            val db = DB.newEmbeddedDB(configBuilder.build())
            db.start()
            embeddedDb = db

            val url =
                "jdbc:mariadb://localhost:$port/$dbName?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
            db.createDB(dbName)

            listOf(url, "org.mariadb.jdbc.Driver", "root", "")
        }

        DatabaseProvider.H2_MEMORY -> {
            val dbName = dbConfig.propertyOrNull("name")?.getString() ?: "rentmycar"
            listOf(
                "jdbc:h2:mem:$dbName;DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE",
                "org.h2.Driver",
                "sa",
                ""
            )
        }

        DatabaseProvider.H2_FILE -> {
            val dbName = dbConfig.propertyOrNull("name")?.getString() ?: "rentmycar"
            val dbPath = dbConfig.propertyOrNull("path")?.getString() ?: "build/h2db/$dbName"
            
            // H2 requires absolute paths or explicitly relative paths (starting with ./ or ~/)
            val absolutePath = if (dbPath.startsWith("/") || dbPath.startsWith("~/") || dbPath.startsWith("./")) {
                dbPath
            } else {
                File(dbPath).absolutePath
            }
            
            listOf(
                "jdbc:h2:file:$absolutePath;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE",
                "org.h2.Driver",
                "sa",
                ""
            )
        }
    }

    Database.connect(url, driver, user, password)

    val reset = System.getenv("DB_RESET")?.equals("true", ignoreCase = true)
        ?: config.propertyOrNull("db.reset")?.getString()?.equals("true", ignoreCase = true)
        ?: false

    val flyway = Flyway.configure()
        .dataSource(url, user, password)
        .locations("classpath:migrations")
        .cleanDisabled(false)
        .load()

    when {
        reset -> {
            flyway.clean()
            flyway.migrate()
        }
        else -> {
            flyway.migrate()
        }
    }
}

private fun isPortInUse(port: Int): Boolean {
    return try {
        Socket("localhost", port).use { true }
    } catch (e: Exception) {
        false
    }
}

fun stopEmbeddedDatabase() {
    embeddedDb?.let { db ->
        try {
            db.stop()
            embeddedDb = null
        } catch (e: Exception) {
            System.err.println("Error stopping embedded database: ${e.message}")
        }
    }
}

/**
 * Executes a database query on the IO dispatcher.
 * Wraps Exposed transaction for safe concurrent access.
 */
suspend fun <T> dbQuery(block: () -> T): T =
    withContext(Dispatchers.IO) {
        transaction { block() }
    }
