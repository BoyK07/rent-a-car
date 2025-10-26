package dev.koenv.rentmycar.routes.api.v1.availability

import dev.koenv.rentmycar.domain.enums.Role
import dev.koenv.rentmycar.domain.service.CarAvailabilityService
import dev.koenv.rentmycar.mappers.car.*
import dev.koenv.rentmycar.routes.RouteRegistrar
import dev.koenv.rentmycar.shared.http.ApiException
import dev.koenv.rentmycar.shared.util.requireLocalDateTimeParamOrNull
import dev.koenv.rentmycar.shared.util.requireRole
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import java.util.UUID

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
							UUID.fromString(it)
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

