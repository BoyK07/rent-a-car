package dev.koenv.rentmycar.plugins

import dev.koenv.rentmycar.domain.services.CityService
import dev.koenv.rentmycar.domain.services.UserService
import dev.koenv.rentmycar.storage.repositories.CityRepositoryImpl
import dev.koenv.rentmycar.storage.repositories.UserRepositoryImpl
import io.ktor.server.application.*
import io.ktor.util.AttributeKey

private val ServicesKey = AttributeKey<Services>("services")

data class Services(
    val city: CityService,
    val user: UserService
)

fun Application.configureServices() {
    if (!attributes.contains(ServicesKey)) {
        val db = database()
        val services = Services(
            city = CityService(CityRepositoryImpl(db)),
            user = UserService(UserRepositoryImpl(db))
        )
        attributes.put(ServicesKey, services)
        log.info("Services initialized")
    }
}

fun Application.services(): Services = attributes[ServicesKey]
fun Application.cityService(): CityService = services().city
fun Application.userService(): UserService = services().user
