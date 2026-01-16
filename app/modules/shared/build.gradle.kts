import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.sqldelight)
}

group = "dev.koenv.rentmycar.shared"
version = "1.0.0"

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    jvm { 
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    sourceSets {
        val commonMain = sourceSets.getByName("commonMain")
        val commonTest = sourceSets.getByName("commonTest")
        val androidMain = sourceSets.getByName("androidMain")
        val jvmMain = sourceSets.getByName("jvmMain")

        all {
            languageSettings.optIn("kotlin.uuid.ExperimentalUuidApi")
        }

        commonMain.dependencies {
            // =====================================
            // Kotlinx - Multiplatform
            // =====================================
            api(libs.bignum)
            api(libs.kotlinx.serialization.json)
            api(libs.kotlinx.datetime)
            api(libs.kotlinx.coroutines.core)

            // =====================================
            // Ktor - Common
            // =====================================
            api(libs.ktor.http)
            api(libs.ktor.resources)
            
            // =====================================
            // Ktor - Client
            // =====================================
            api(libs.ktor.client.core)
            api(libs.ktor.client.content.negotiation)
            api(libs.ktor.client.auth)
            api(libs.ktor.client.logging)
            api(libs.ktor.client.resources)
            api(libs.ktor.serialization.kotlinx.json.multiplatform)
            
            // =====================================
            // Multiplatform Storage
            // =====================================
            api(libs.multiplatform.settings)
            api(libs.multiplatform.settings.no.arg)
            
            // =====================================
            // SQLDelight - Database
            // =====================================
            api(libs.sqldelight.runtime)
            api(libs.sqldelight.coroutines)
        }
        
        androidMain.dependencies {
            // =====================================
            // Ktor - Android Engine
            // =====================================
            implementation(libs.ktor.client.okhttp)
            
            // =====================================
            // SQLDelight - Android Driver
            // =====================================
            implementation(libs.sqldelight.android.driver)
        }
        
        jvmMain.dependencies {
            // =====================================
            // Ktor - JVM Engine
            // =====================================
            implementation(libs.ktor.client.cio)
            
            // =====================================
            // SQLDelight - JVM Driver
            // =====================================
            implementation(libs.sqldelight.jvm.driver)
        }

        commonTest.dependencies {
            // =====================================
            // Testing
            // =====================================
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "dev.koenv.rentmycar.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}

sqldelight {
    databases {
        create("RentMyCarDatabase") {
            packageName.set("dev.koenv.rentmycar.shared.db")
        }
    }
}
