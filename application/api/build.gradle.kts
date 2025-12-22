val ktor_version: String by project
val exposed_version: String by project
val mariadb_version: String by project
val argon2_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val prometheus_version: String by project
val hikari_version: String by project
val flyway_version: String by project
val koin_version: String by project

plugins {
    kotlin("jvm") version "2.3.0"
    id("io.ktor.plugin") version "3.3.1"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.20"
}

group = "dev.koenv"
version = "0.0.1"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

tasks.register<JavaExec>("generateMigrations") {
    group = "database"
    description = "Auto-discovers Exposed tables and generates migration SQL scripts"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("dev.koenv.rentmycar.storage.db.MigrationGenerator")
}

repositories {
    mavenCentral()
}

dependencies {

    // --- Ktor Server Core ---
    implementation("io.ktor:ktor-server-core:${ktor_version}")
    implementation("io.ktor:ktor-server-netty:${ktor_version}")
    implementation("io.ktor:ktor-server-host-common:${ktor_version}")
    implementation("io.ktor:ktor-server-default-headers:${ktor_version}")
    implementation("io.ktor:ktor-server-compression:${ktor_version}")
    implementation("io.ktor:ktor-server-cors:${ktor_version}")
    implementation("io.ktor:ktor-server-content-negotiation:${ktor_version}")
    implementation("io.ktor:ktor-server-call-logging:${ktor_version}")
    implementation("io.ktor:ktor-server-call-id:${ktor_version}")
    implementation("io.ktor:ktor-server-status-pages:${ktor_version}")
    implementation("io.ktor:ktor-server-request-validation:${ktor_version}")
    implementation("io.ktor:ktor-server-resources:${ktor_version}")
    implementation("io.ktor:ktor-server-metrics-micrometer")
    implementation("io.ktor:ktor-server-config-yaml:${ktor_version}")
    implementation("io.ktor:ktor-serialization-kotlinx-json:${ktor_version}")

    // --- Authentication ---
    implementation("io.ktor:ktor-server-auth:${ktor_version}")
    implementation("io.ktor:ktor-server-auth-jwt:${ktor_version}")
    implementation("de.mkammerer:argon2-jvm:${argon2_version}")

    // --- Metrics & Monitoring ---
    implementation("io.micrometer:micrometer-registry-prometheus:$prometheus_version")

    // --- Rate Limiting (3rd Party) ---
    implementation("io.github.flaxoos:ktor-server-rate-limiting:2.2.1")

    // --- Database: Exposed ORM ---
    implementation("org.jetbrains.exposed:exposed-core:${exposed_version}")
    implementation("org.jetbrains.exposed:exposed-dao:${exposed_version}")
    implementation("org.jetbrains.exposed:exposed-jdbc:${exposed_version}")

    // --- Database: Exposed ORM Migration support ---
    implementation("org.jetbrains.exposed:exposed-migration-core:${exposed_version}")
    implementation("org.jetbrains.exposed:exposed-migration-jdbc:${exposed_version}")
    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:${exposed_version}")

    // --- Database Drivers ---
    implementation("ch.vorburger.mariaDB4j:mariaDB4j:${mariadb_version}")
    implementation("org.mariadb.jdbc:mariadb-java-client:${mariadb_version}")

    // --- Connection Pool ---
    implementation("com.zaxxer:HikariCP:$hikari_version")

    // --- Database Migrations ---
    implementation("org.flywaydb:flyway-core:$flyway_version")
    implementation("org.flywaydb:flyway-mysql:$flyway_version")

    // -- Dependency Injection ---
    implementation("io.insert-koin:koin-ktor:$koin_version")

    // --- Logging ---
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("io.insert-koin:koin-logger-slf4j:$koin_version")

    // --- Testing ---
    testImplementation("io.ktor:ktor-server-test-host:${ktor_version}")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
    testImplementation("io.insert-koin:koin-test-junit5:$koin_version")

}
