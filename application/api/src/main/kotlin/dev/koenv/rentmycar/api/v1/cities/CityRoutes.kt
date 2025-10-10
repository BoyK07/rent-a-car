package dev.koenv.rentmycar.api.v1.cities

import dev.koenv.rentmycar.domain.model.City
import dev.koenv.rentmycar.domain.services.CityService
import dev.koenv.rentmycar.storage.repositories.CityRepositoryImpl
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

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