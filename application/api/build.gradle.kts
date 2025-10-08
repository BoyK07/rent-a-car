val exposed_version: String by project
val h2_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val mysql_version: String by project
val prometheus_version: String by project
val hikari_version: String by project
val flyway_version: String by project

plugins {
    kotlin("jvm") version "2.2.20"
    id("io.ktor.plugin") version "3.2.3"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.20"
}

group = "dev.koenv"
version = "0.0.1"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

repositories {
    mavenCentral()
}

dependencies {

    // --- Ktor Server Core ---
    implementation("io.ktor:ktor-server-core")
    implementation("io.ktor:ktor-server-netty")
    implementation("io.ktor:ktor-server-host-common")
    implementation("io.ktor:ktor-server-default-headers")
    implementation("io.ktor:ktor-server-compression")
    implementation("io.ktor:ktor-server-cors")
    implementation("io.ktor:ktor-server-content-negotiation")
    implementation("io.ktor:ktor-server-call-logging")
    implementation("io.ktor:ktor-server-call-id")
    implementation("io.ktor:ktor-server-status-pages")
    implementation("io.ktor:ktor-server-request-validation")
    implementation("io.ktor:ktor-server-auth")
    implementation("io.ktor:ktor-server-auth-jwt")
    implementation("io.ktor:ktor-server-resources")
    implementation("io.ktor:ktor-server-metrics-micrometer")
    implementation("io.ktor:ktor-server-config-yaml")
    implementation("io.ktor:ktor-serialization-kotlinx-json")

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

    // --- Database Drivers ---
    implementation("com.h2database:h2:$h2_version")              // Local dev/testing
    implementation("mysql:mysql-connector-j:$mysql_version") // Production/MySQL

    // --- Connection Pool ---
    implementation("com.zaxxer:HikariCP:$hikari_version")

    // --- Database Migrations ---
    implementation("org.flywaydb:flyway-core:$flyway_version")
    implementation("org.flywaydb:flyway-mysql:$flyway_version")

    // --- Logging ---
    implementation("ch.qos.logback:logback-classic:$logback_version")

    // --- Testing ---
    testImplementation("io.ktor:ktor-server-test-host")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}
