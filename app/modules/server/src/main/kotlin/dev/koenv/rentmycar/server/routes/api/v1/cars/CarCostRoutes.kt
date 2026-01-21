package dev.koenv.rentmycar.server.routes.api.v1.cars

/**
 * Car cost calculation API routes.
 * 
 * All endpoints require authentication.
 * 
 * Endpoints:
 * - POST /api/v1/cars/{id}/cost/total - Calculate total cost of ownership
 * - POST /api/v1/cars/{id}/cost/per-hour - Calculate hourly rate recommendation
 */

import dev.koenv.rentmycar.server.domain.service.CarService
import dev.koenv.rentmycar.server.routes.RouteRegistrar
import dev.koenv.rentmycar.server.util.requireRole
import dev.koenv.rentmycar.server.util.respondSuccess
import dev.koenv.rentmycar.server.util.respondError
import dev.koenv.rentmycar.shared.domain.enums.Role
import dev.koenv.rentmycar.shared.dto.car.CarCostPerKmResponseDto
import dev.koenv.rentmycar.shared.dto.car.CarTcoResponseDto
import dev.koenv.rentmycar.shared.resources.ApiV1
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.resources.get
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import kotlin.uuid.Uuid

object CarCostRoutes : RouteRegistrar {
    override fun Route.register() {
        val carService by inject<CarService>()

        authenticate("auth-jwt") {
            get<ApiV1.Cars.Id.Tco> { resource ->
                call.requireRole(Role.ADMIN, Role.DRIVER, Role.MEMBER)
                val id = Uuid.parse(resource.parent.id)

                val car = carService.getById(id)
                if (car == null) {
                    return@get call.respondError(
                        HttpStatusCode.NotFound,
                        "Car not found",
                        "CAR_NOT_FOUND",
                        call.callId
                    )
                }

                val tco = carService.calculateTcoPerYear(car, resource.annualKm)
                call.respondSuccess(CarTcoResponseDto(carId = id, annualKm = resource.annualKm, tcoPerYear = tco))
            }

            get<ApiV1.Cars.Id.CostPerKm> { resource ->
                call.requireRole(Role.ADMIN, Role.DRIVER, Role.MEMBER)
                val id = Uuid.parse(resource.parent.id)

                val car = carService.getById(id)
                if (car == null) {
                    return@get call.respondError(
                        HttpStatusCode.NotFound,
                        "Car not found",
                        "CAR_NOT_FOUND",
                        call.callId
                    )
                }

                val cost = carService.calculateCostPerKm(car)
                call.respondSuccess(CarCostPerKmResponseDto(carId = id, costPerKm = cost))
            }
        }
    }
}

