package dev.koenv.rentmycar.storage.db

import org.flywaydb.core.Flyway
import org.h2.jdbcx.JdbcDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SchemaUtils.createStatements
import org.jetbrains.exposed.sql.Table
import java.io.File

object MigrationGenerator {

    private val tables: List<Table> = listOf(
        dev.koenv.rentmycar.storage.db.tables.UsersTable,
        dev.koenv.rentmycar.storage.db.tables.CitiesTable
    )

    private val migrationsDir = File("src/main/resources/migrations")

    fun generate() {
        val h2 = JdbcDataSource().apply {
            setURL("jdbc:h2:mem:diff;MODE=PostgreSQL;DB_CLOSE_DELAY=-1")
            user = "sa"
            password = ""
        }

        val db = Database.connect(h2)

        transaction(db) {
            SchemaUtils.createMissingTablesAndColumns(*tables.toTypedArray())

            val schemaSQL = createStatements(*tables.toTypedArray()).joinToString(";\n")
            val version = nextVersion()
            val filename = "V${version}__auto_generated.sql"
            val file = File(migrationsDir, filename)

            file.writeText(schemaSQL + ";\n")
            println("✅ Migration generated: ${file.path}")
        }
    }

    fun migrate() {
        val flyway = Flyway.configure()
            .dataSource("jdbc:postgresql://localhost:5432/yourdb", "user", "password")
            .locations("classpath:migrations")
            .load()
        flyway.migrate()
        println("✅ Database migrated successfully.")
    }

    private fun nextVersion(): Int {
        val files = migrationsDir.listFiles()?.filter { it.name.startsWith("V") } ?: emptyList()
        val maxVersion = files.mapNotNull {
            Regex("""V(\d+)__""").find(it.name)?.groupValues?.get(1)?.toIntOrNull()
        }.maxOrNull() ?: 0
        return maxVersion + 1
    }
}
