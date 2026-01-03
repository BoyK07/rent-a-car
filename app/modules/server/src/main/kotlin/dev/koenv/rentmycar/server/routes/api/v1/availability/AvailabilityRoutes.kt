package dev.koenv.rentmycar.server.routes.api.v1.availability

import dev.koenv.rentmycar.server.domain.service.CarAvailabilityService
import dev.koenv.rentmycar.server.mappers.car.toAvailabilityDto
import dev.koenv.rentmycar.server.routes.RouteRegistrar
import dev.koenv.rentmycar.server.util.requireLocalDateTimeParamOrNull
import dev.koenv.rentmycar.server.util.requireRole
import dev.koenv.rentmycar.shared.domain.enums.Role
import dev.koenv.rentmycar.shared.http.ApiException
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import kotlin.uuid.Uuid

object AvailabilityRoutes : RouteRegistrar {
    override fun Route.register() {
        val availabilityService by inject<CarAvailabilityService>()

        route("/availability") {
            authenticate("auth-jwt") {
                // GET /api/v1/availability - List availability windows (filters: carId, start, end)
                get {
                    call.requireRole(Role.ADMIN, Role.DRIVER, Role.MEMBER)

                    val carId = call.request.queryParameters["carId"]?.let {
                        try {
                            Uuid.parse(it)
                        } catch (_: IllegalArgumentException) {
                            throw ApiException(
                                HttpStatusCode.BadRequest,
                                message = "Invalid carId format"
                            )
                        }
                    }

                    val start = call.requireLocalDateTimeParamOrNull("start")
                    val end = call.requireLocalDateTimeParamOrNull("end")

                    val items = availabilityService.listFiltered(
                        carId = carId,
                        startTime = start,
                        endTime = end
                    )

                    call.respond(items.map { it.toAvailabilityDto() })
                }
            }
        }
    }
}

