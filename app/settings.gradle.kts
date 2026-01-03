rootProject.name = "rentmycar"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

include(":app")
include(":server")
include(":shared")

project(":app").projectDir = file("modules/app")
project(":server").projectDir = file("modules/server")
project(":shared").projectDir = file("modules/shared")