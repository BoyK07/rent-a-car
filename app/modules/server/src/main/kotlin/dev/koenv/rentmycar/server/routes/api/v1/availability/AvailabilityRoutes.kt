package dev.koenv.rentmycar.server.routes.api.v1.availability

/**
 * Car availability search API routes.
 * 
 * Endpoints:
 * - GET /api/v1/availability - Search cars available in time range and location
 */

import dev.koenv.rentmycar.server.domain.service.CarAvailabilityService
import dev.koenv.rentmycar.server.mappers.car.toAvailabilityDto
import dev.koenv.rentmycar.server.routes.RouteRegistrar
import dev.koenv.rentmycar.server.util.requireRole
import dev.koenv.rentmycar.server.util.respondSuccess
import dev.koenv.rentmycar.server.util.respondError
import dev.koenv.rentmycar.shared.domain.enums.Role
import dev.koenv.rentmycar.shared.resources.ApiV1
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.resources.get
import io.ktor.server.routing.*
import kotlinx.datetime.LocalDateTime
import org.koin.ktor.ext.inject
import kotlin.uuid.Uuid

object AvailabilityRoutes : RouteRegistrar {
    override fun Route.register() {
        val availabilityService by inject<CarAvailabilityService>()

        authenticate("auth-jwt") {
            // GET /api/v1/availability - List availability windows (filters: carId, start, end)
            get<ApiV1.Availability> { resource ->
                call.requireRole(Role.ADMIN, Role.DRIVER, Role.MEMBER)

                val carId = try {
                    Uuid.parse(resource.carId)
                } catch (_: IllegalArgumentException) {
                    return@get call.respondError(
                        HttpStatusCode.BadRequest,
                        "Invalid carId format",
                        "INVALID_CAR_ID",
                        call.callId
                    )
                }

                val start = try {
                    LocalDateTime.parse(resource.start)
                } catch (_: IllegalArgumentException) {
                    return@get call.respondError(
                        HttpStatusCode.BadRequest,
                        "Invalid start date format",
                        "INVALID_START_DATE",
                        call.callId
                    )
                }
                
                val end = try {
                    LocalDateTime.parse(resource.end)
                } catch (_: IllegalArgumentException) {
                    return@get call.respondError(
                        HttpStatusCode.BadRequest,
                        "Invalid end date format",
                        "INVALID_END_DATE",
                        call.callId
                    )
                }

                val items = availabilityService.listFiltered(
                    carId = carId,
                    startTime = start,
                    endTime = end
                )

                call.respondSuccess(items.map { it.toAvailabilityDto() })
            }
        }
    }
}

