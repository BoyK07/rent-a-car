package dev.koenv.rentmycar.api.v1.cities

import dev.koenv.rentmycar.domain.service.CityService
import dev.koenv.rentmycar.storage.repository.CityRepositoryImpl
import io.ktor.server.routing.*

fun Route.cityRoutes() {
    val service = CityService(CityRepositoryImpl())

    route("/cities") {
        post {
            TODO("Not yet implemented")
        }

        get("/{id}") {
            TODO("Not yet implemented")
        }

        put("/{id}") {
            TODO("Not yet implemented")
        }

        delete("/{id}") {
            TODO("Not yet implemented")
        }
    }
}