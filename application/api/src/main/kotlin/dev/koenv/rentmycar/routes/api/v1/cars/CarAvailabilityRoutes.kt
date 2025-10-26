package dev.koenv.rentmycar.routes.api.v1.cars

import dev.koenv.rentmycar.domain.enums.Role
import dev.koenv.rentmycar.domain.service.CarAvailabilityService
import dev.koenv.rentmycar.domain.service.CarService
import dev.koenv.rentmycar.dto.car.CreateCarAvailabilityRequestDto
import dev.koenv.rentmycar.dto.car.PatchCarAvailabilityRequestDto
import dev.koenv.rentmycar.dto.car.UpdateCarAvailabilityRequestDto
import dev.koenv.rentmycar.mappers.car.*
import dev.koenv.rentmycar.routes.RouteRegistrar
import dev.koenv.rentmycar.shared.http.ApiException
import dev.koenv.rentmycar.shared.util.requireBodyOrFail
import dev.koenv.rentmycar.shared.util.requireRole
import dev.koenv.rentmycar.shared.util.requireUuidParamOrFail
import io.ktor.http.*
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import java.util.UUID

object CarAvailabilityRoutes : RouteRegistrar {
	override fun Route.register() {
		val availabilityService by inject<CarAvailabilityService>()
		val carService by inject<CarService>()

		route("/cars") {
			authenticate("auth-jwt") {
				route("/{id}/availability") {

					// GET /api/v1/cars/{id}/availability - List availability windows for a car
					get {
						call.requireRole(Role.ADMIN, Role.DRIVER, Role.MEMBER)
						val carId = call.requireUuidParamOrFail("id")
						val items = availabilityService.getByCarId(carId)
						call.respond(items.map { it.toAvailabilityDto() })
					}

					// GET /api/v1/cars/{id}/availability/{availabilityId} - Get specific availability window
					get("/{availabilityId}") {
						call.requireRole(Role.ADMIN, Role.DRIVER, Role.MEMBER)
						val carId = call.requireUuidParamOrFail("id")
						val availabilityId = call.requireUuidParamOrFail("availabilityId")

						val availability = availabilityService.getById(availabilityId)
							?: throw ApiException(HttpStatusCode.NotFound, message = "Availability window not found")

						if (availability.carId != carId) {
							throw ApiException(HttpStatusCode.NotFound, message = "Availability does not belong to this car")
						}

						call.respond(availability.toAvailabilityDto())
					}

					// POST /api/v1/cars/{id}/availability - Add availability window for a car
					post {
						val principal = call.requireRole(Role.ADMIN, Role.DRIVER)
						val userId = UUID.fromString(principal.payload.getClaim("userId").asString())
						val role = Role.valueOf(principal.payload.getClaim("role").asString())

						val carId = call.requireUuidParamOrFail("id")
						val car = carService.getById(carId)
							?: throw ApiException(HttpStatusCode.NotFound, message = "Car not found")

						// Ownership check
						if (role != Role.ADMIN && car.ownerId != userId) {
							throw ApiException(HttpStatusCode.Forbidden, message = "You are not the owner of this car")
						}

						val req = call.requireBodyOrFail<CreateCarAvailabilityRequestDto>()
						val created = availabilityService.create(req.toAvailabilityEntity(carId))
						call.respond(HttpStatusCode.Created, created.toAvailabilityDto())
					}

					// PUT /api/v1/cars/{id}/availability/{availabilityId} - Replace availability window
					put("/{availabilityId}") {
						val principal = call.requireRole(Role.ADMIN, Role.DRIVER)
						val userId = UUID.fromString(principal.payload.getClaim("userId").asString())
						val role = Role.valueOf(principal.payload.getClaim("role").asString())

						val carId = call.requireUuidParamOrFail("id")
						val availabilityId = call.requireUuidParamOrFail("availabilityId")

						val availability = availabilityService.getById(availabilityId)
							?: throw ApiException(HttpStatusCode.NotFound, message = "Availability window not found")

						if (availability.carId != carId) {
							throw ApiException(HttpStatusCode.NotFound, message = "Availability does not belong to this car")
						}

						val car = carService.getById(carId)
							?: throw ApiException(HttpStatusCode.NotFound, message = "Car not found")

						// Ownership check
						if (role != Role.ADMIN && car.ownerId != userId) {
							throw ApiException(HttpStatusCode.Forbidden, message = "You are not the owner of this car")
						}

						val req = call.requireBodyOrFail<UpdateCarAvailabilityRequestDto>()
						val updated = availabilityService.update(availabilityId, req.toAvailabilityEntity(availabilityId, carId))
							?: throw ApiException(HttpStatusCode.InternalServerError, message = "Failed to update availability window")

						call.respond(updated.toAvailabilityDto())
					}

					// PATCH /api/v1/cars/{id}/availability/{availabilityId} - Partially update availability window
					patch("/{availabilityId}") {
						val principal = call.requireRole(Role.ADMIN, Role.DRIVER)
						val userId = UUID.fromString(principal.payload.getClaim("userId").asString())
						val role = Role.valueOf(principal.payload.getClaim("role").asString())

						val carId = call.requireUuidParamOrFail("id")
						val availabilityId = call.requireUuidParamOrFail("availabilityId")

						val availability = availabilityService.getById(availabilityId)
							?: throw ApiException(HttpStatusCode.NotFound, message = "Availability window not found")

						if (availability.carId != carId) {
							throw ApiException(HttpStatusCode.NotFound, message = "Availability does not belong to this car")
						}

						val car = carService.getById(carId)
							?: throw ApiException(HttpStatusCode.NotFound, message = "Car not found")

						// Ownership check
						if (role != Role.ADMIN && car.ownerId != userId) {
							throw ApiException(HttpStatusCode.Forbidden, message = "You are not the owner of this car")
						}

						val req = call.requireBodyOrFail<PatchCarAvailabilityRequestDto>()
						val patched = req.applyAvailabilityPatch(availability)
						val saved = availabilityService.update(availabilityId, patched)
							?: throw ApiException(HttpStatusCode.InternalServerError, message = "Failed to update availability window")

						call.respond(saved.toAvailabilityDto())
					}

					// DELETE /api/v1/cars/{id}/availability/{availabilityId} - Delete availability window
					delete("/{availabilityId}") {
						val principal = call.requireRole(Role.ADMIN, Role.DRIVER)
						val userId = UUID.fromString(principal.payload.getClaim("userId").asString())
						val role = Role.valueOf(principal.payload.getClaim("role").asString())

						val carId = call.requireUuidParamOrFail("id")
						val availabilityId = call.requireUuidParamOrFail("availabilityId")

						val availability = availabilityService.getById(availabilityId)
							?: throw ApiException(HttpStatusCode.NotFound, message = "Availability window not found")

						if (availability.carId != carId) {
							throw ApiException(HttpStatusCode.NotFound, message = "Availability does not belong to this car")
						}

						val car = carService.getById(carId)
							?: throw ApiException(HttpStatusCode.NotFound, message = "Car not found")

						// Ownership check
						if (role != Role.ADMIN && car.ownerId != userId) {
							throw ApiException(HttpStatusCode.Forbidden, message = "You are not the owner of this car")
						}

						if (!availabilityService.delete(availabilityId)) {
							throw ApiException(HttpStatusCode.InternalServerError, message = "Failed to delete availability window")
						}

						call.respond(HttpStatusCode.NoContent)
					}
				}
			}
		}
	}
}

