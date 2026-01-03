plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    application
}

group = "dev.koenv.rentmycar.server"
version = "1.0.0"

kotlin {
    sourceSets {
        all {
            languageSettings.optIn("kotlin.uuid.ExperimentalUuidApi")
        }
    }
}

application {
    mainClass.set("io.ktor.server.netty.EngineMain")

    val isDevelopment = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

dependencies {
    implementation(projects.shared)

    // =====================================
    // Ktor - Core Server
    // =====================================
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.host.common)

    // =====================================
    // Ktor - Server Features
    // =====================================
    implementation(libs.ktor.server.default.headers)
    implementation(libs.ktor.server.compression)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.call.id)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.server.request.validation)
    implementation(libs.ktor.server.resources)
    implementation(libs.ktor.server.config.yaml)

    // =====================================
    // Ktor - Serialization
    // =====================================
    implementation(libs.ktor.serialization.kotlinx.json)

    // =====================================
    // Ktor - Authentication
    // =====================================
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)

    // =====================================
    // Security
    // =====================================
    implementation(libs.argon2)

    // =====================================
    // Metrics & Monitoring
    // =====================================
    implementation(libs.ktor.server.metrics.micrometer)
    implementation(libs.micrometer.registry.prometheus)

    // =====================================
    // Rate Limiting
    // =====================================
    implementation(libs.ktor.server.rate.limiting)

    // =====================================
    // Database - Exposed ORM
    // =====================================
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.migration.core)
    implementation(libs.exposed.migration.jdbc)
    implementation(libs.exposed.kotlin.datetime)

    // =====================================
    // Database - Drivers
    // =====================================
    implementation(libs.mariadb4j)
    implementation(libs.mariadb.jdbc)

    // =====================================
    // Database - Connection Pooling
    // =====================================
    implementation(libs.hikari.cp)

    // =====================================
    // Database - Migrations
    // =====================================
    implementation(libs.flyway.core)
    implementation(libs.flyway.mysql)

    // =====================================
    // Dependency Injection
    // =====================================
    implementation(libs.koin.ktor)
    implementation(libs.koin.logger.slf4j)

    // =====================================
    // Logging
    // =====================================
    implementation(libs.logback.classic)

    // =====================================
    // Testing
    // =====================================
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.koin.test.junit5)
}

tasks.register<JavaExec>("generateMigrations") {
    group = "database"
    description = "Auto-discovers Exposed tables and generates migration SQL scripts"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("dev.koenv.rentmycar.server.storage.db.MigrationGenerator")
}
