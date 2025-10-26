package dev.koenv.rentmycar.routes.api.v1.cars

import dev.koenv.rentmycar.domain.enums.Role
import dev.koenv.rentmycar.domain.service.CarService
import dev.koenv.rentmycar.dto.car.CarCostPerKmResponseDto
import dev.koenv.rentmycar.dto.car.CarTcoResponseDto
import dev.koenv.rentmycar.routes.RouteRegistrar
import dev.koenv.rentmycar.shared.http.ApiException
import dev.koenv.rentmycar.shared.util.requireRole
import dev.koenv.rentmycar.shared.util.requireUuidParamOrFail
import io.ktor.http.*
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

object CarCostRoutes : RouteRegistrar {
    override fun Route.register() {
        val carService by inject<CarService>()

        route("/cars") {
            authenticate("auth-jwt") {
                get("/{id}/tco") {
                    call.requireRole(Role.ADMIN, Role.DRIVER, Role.MEMBER)
                    val id = call.requireUuidParamOrFail("id")
                    val annualKm = call.request.queryParameters["annualKm"]?.toIntOrNull()
                        ?: throw ApiException(HttpStatusCode.BadRequest, message = "Annual km is required")

                    val car = carService.getById(id)
                        ?: throw ApiException(HttpStatusCode.NotFound, message = "Car not found")

                    val tco = carService.calculateTcoPerYear(car, annualKm)
                    call.respond(CarTcoResponseDto(carId = id, annualKm = annualKm, tcoPerYear = tco))
                }

                get("/{id}/cost-per-km") {
                    call.requireRole(Role.ADMIN, Role.DRIVER, Role.MEMBER)
                    val id = call.requireUuidParamOrFail("id")

                    val car = carService.getById(id)
                        ?: throw ApiException(HttpStatusCode.NotFound, message = "Car not found")

                    val cost = carService.calculateCostPerKm(car)
                    call.respond(CarCostPerKmResponseDto(carId = id, costPerKm = cost))
                }
            }
        }
    }
}


