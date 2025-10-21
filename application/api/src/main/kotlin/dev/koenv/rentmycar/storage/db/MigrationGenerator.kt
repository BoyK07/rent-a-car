@file:OptIn(org.jetbrains.exposed.v1.core.ExperimentalDatabaseMigrationApi::class)

package dev.koenv.rentmycar.storage.db

import ch.vorburger.mariadb4j.DB
import ch.vorburger.mariadb4j.DBConfigurationBuilder
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.migration.jdbc.MigrationUtils
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.reflect.full.isSubclassOf

object MigrationGenerator {

    private fun discoverTables(pkg: String): List<Table> {
        val out = mutableListOf<Table>()
        val pkgPath = pkg.replace('.', '/')
        val cl = Thread.currentThread().contextClassLoader
        val resources = cl.getResources(pkgPath)
        while (resources.hasMoreElements()) {
            val url = resources.nextElement()
            val dir = File(url.file)
            if (!dir.exists()) continue
            dir.walkTopDown()
                .filter { it.extension == "class" }
                .forEach { f ->
                    val className = "$pkg.${f.nameWithoutExtension}"
                    try {
                        val kClass = Class.forName(className).kotlin
                        if (kClass.isSubclassOf(Table::class)) {
                            kClass.objectInstance?.let { out += it as Table }
                        }
                    } catch (_: Exception) {
                        // ignore classes that can't be loaded/reflected
                    }
                }
        }
        return out
    }

    private fun randomName(): String {
        val adjectives = listOf("sleepy", "premium", "angry", "cosmic", "brave", "fuzzy", "silent", "vivid", "ancient", "spicy")
        val nouns = listOf("otter", "mister", "falcon", "cactus", "nebula", "fear", "pizza", "vortex", "wizard", "crab")
        return "${adjectives.random()}_${nouns.random()}"
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val tmpRoot = createTempDirectory("mariadb4j-migrations").toFile()
        val baseDir = File(tmpRoot, "base")
        val dataDir = File(tmpRoot, "data")

        val config = DBConfigurationBuilder.newBuilder()
            .setPort(0)
            .setBaseDir(baseDir)
            .setDataDir(dataDir)
            .setDeletingTemporaryBaseAndDataDirsOnShutdown(true)
            .build()

        val dbServer: DB = DB.newEmbeddedDB(config)
        try {
            dbServer.start()
            val port = config.port

            val dbName = "rentmycar"
            val url = "jdbc:mariadb://localhost:$port/$dbName?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
            val driver = "org.mariadb.jdbc.Driver"
            val user = "root"
            val pass = ""

            dbServer.run("CREATE DATABASE IF NOT EXISTS $dbName;")

            // Run Flyway migrations present in resources
            Flyway.configure()
                .dataSource(url, user, pass)
                .locations("classpath:migrations")
                .load()
                .migrate()

            val db = Database.connect(url, driver, user, pass)

            val tables = discoverTables("${this.javaClass.packageName}.tables")
            println("Discovered ${tables.size} tables")

            val requiredStatements = transaction(db) {
                MigrationUtils.statementsRequiredForDatabaseMigration(*tables.toTypedArray(), withLogs = true)
            }

            // Apply required statements so subsequent drop detection sees added columns/indices
            if (requiredStatements.isNotEmpty()) {
                transaction(db) {
                    requiredStatements.forEach { sql -> exec(sql) }
                }
            }

            val dropStatements = transaction(db) {
                buildList {
                    addAll(MigrationUtils.dropUnmappedColumnsStatements(*tables.toTypedArray(), withLogs = true))
                    addAll(MigrationUtils.dropUnmappedIndices(*tables.toTypedArray(), withLogs = true))
                    addAll(MigrationUtils.dropUnmappedSequences(*tables.toTypedArray(), withLogs = true))
                }
            }

            val allStatements = buildList {
                addAll(requiredStatements)
                addAll(dropStatements)
            }

            val outDir = File("src/main/resources/migrations").apply { mkdirs() }
            if (allStatements.isNotEmpty()) {
                val scriptName = "V${System.currentTimeMillis()}__${randomName()}.sql"
                File(outDir, scriptName).writeText(allStatements.joinToString(";\n", postfix = ";"))
                println("Migration script written: ${scriptName}")
            } else {
                println("No schema changes detected.")
            }
        } finally {
            try { dbServer.stop() } catch (_: Exception) {}
            try { tmpRoot.deleteRecursively() } catch (_: Exception) {}
        }
    }
}