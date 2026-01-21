package dev.koenv.rentmycar

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform