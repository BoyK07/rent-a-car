@file:OptIn(org.jetbrains.exposed.v1.core.ExperimentalDatabaseMigrationApi::class)

package dev.koenv.rentmycar.storage.db

import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.migration.jdbc.MigrationUtils
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.jvmName
import java.io.File

object MigrationGenerator {
    private fun discoverTables(packageName: String): List<Table> {
        val tables = mutableListOf<Table>()
        val pkgPath = packageName.replace('.', '/')
        val cl = Thread.currentThread().contextClassLoader
        val resources = cl.getResources(pkgPath)
        while (resources.hasMoreElements()) {
            val url = resources.nextElement()
            val dir = File(url.file)
            if (!dir.exists()) continue
            dir.walkTopDown()
                .filter { it.extension == "class" }
                .forEach { f ->
                    val className = "$packageName.${f.nameWithoutExtension}"
                    try {
                        val kClass = Class.forName(className).kotlin
                        if (kClass.isSubclassOf(Table::class)) {
                            kClass.objectInstance?.let { tables += it as Table }
                        }
                    } catch (t: Throwable) {
                        println("Failed to load class $className: ${t.message}")
                    }
                }
        }
        return tables
    }

    private fun randomName(): String {
        val adjectives = listOf("sleepy", "premium", "angry", "cosmic", "brave", "fuzzy", "silent", "vivid", "ancient", "spicy")
        val nouns = listOf("otter", "mister", "falcon", "cactus", "nebula", "fear", "pizza", "vortex", "wizard", "crab")
        return "${adjectives.random()}_${nouns.random()}"
    }

    @JvmStatic
    fun main(args: Array<String>) {
        // Scratch DB: H2 in-memory, MySQL compatibility
        val url = "jdbc:h2:mem:migrations;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1;"
        val driver = "org.h2.Driver"
        val user = ""
        val pass = ""

        // 1) Recreate current baseline by applying existing SQL migrations into the scratch DB
        Flyway.configure()
            .dataSource(url, user, pass)
            .locations("classpath:migrations") // your existing files
            .load()
            .migrate()

        // 2) Connect Exposed to that scratch DB
        val db = Database.connect(url, driver, user, pass)

        // 3) Discover tables from code
        val tables = discoverTables("dev.koenv.rentmycar.storage.db.tables")
        println("Discovered ${tables.size} tables: ${tables.joinToString { it::class.jvmName }}")

        // Ensure output dir exists
        val outDir = File("src/main/resources/migrations")
        if (!outDir.exists()) outDir.mkdirs()

        // Unique script name: V<timestamp>__<random>.sql
        val randomPart = randomName()
        val timestamp = System.currentTimeMillis()
        val scriptName = "V${timestamp}__${randomPart}"

        // 4) Generate ONLY the delta vs baseline
        transaction(db) {
            MigrationUtils.generateMigrationScript(
                tables = tables.toTypedArray(),
                scriptDirectory = outDir.absolutePath,
                scriptName = scriptName,
                withLogs = true
            )
        }
        println("Migration script written as $scriptName")

    }
}
