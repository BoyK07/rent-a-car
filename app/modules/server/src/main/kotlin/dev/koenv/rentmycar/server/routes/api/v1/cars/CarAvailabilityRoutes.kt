package dev.koenv.rentmycar.server.routes.api.v1.cars

import dev.koenv.rentmycar.server.domain.service.CarAvailabilityService
import dev.koenv.rentmycar.server.domain.service.CarService
import dev.koenv.rentmycar.server.mappers.car.applyAvailabilityPatch
import dev.koenv.rentmycar.server.mappers.car.toAvailabilityDto
import dev.koenv.rentmycar.server.mappers.car.toAvailabilityEntity
import dev.koenv.rentmycar.server.routes.RouteRegistrar
import dev.koenv.rentmycar.server.util.requireBodyOrFail
import dev.koenv.rentmycar.server.util.requireRole
import dev.koenv.rentmycar.server.util.respondError
import dev.koenv.rentmycar.server.util.respondSuccess
import dev.koenv.rentmycar.server.util.respondCreated
import dev.koenv.rentmycar.server.util.verifyOwnership
import dev.koenv.rentmycar.shared.domain.enums.Role
import dev.koenv.rentmycar.shared.dto.car.CreateCarAvailabilityRequestDto
import dev.koenv.rentmycar.shared.dto.car.PatchCarAvailabilityRequestDto
import dev.koenv.rentmycar.shared.dto.car.UpdateCarAvailabilityRequestDto
import dev.koenv.rentmycar.shared.http.ApiException
import dev.koenv.rentmycar.shared.resources.ApiV1
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.resources.put
import io.ktor.server.resources.patch
import io.ktor.server.resources.delete
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import kotlin.uuid.Uuid

object CarAvailabilityRoutes : RouteRegistrar {
    override fun Route.register() {
        val availabilityService by inject<CarAvailabilityService>()
        val carService by inject<CarService>()

        authenticate("auth-jwt") {
            // GET /api/v1/cars/{id}/availability - List availability windows for a car
            get<ApiV1.Cars.Id.Availability> { resource ->
                call.requireRole(Role.ADMIN, Role.DRIVER, Role.MEMBER)
                
                val carId = try {
                    Uuid.parse(resource.parent.id)
                } catch (_: IllegalArgumentException) {
                    return@get call.respondError(
                        HttpStatusCode.BadRequest,
                        "Invalid carId format",
                        "INVALID_CAR_ID",
                        call.callId
                    )
                }
                
                val items = availabilityService.getByCarId(carId)
                call.respondSuccess(items.map { it.toAvailabilityDto() })
            }

            // GET /api/v1/cars/{id}/availability/{availabilityId} - Get specific availability window
            get<ApiV1.Cars.Id.Availability.AvailabilityId> { resource ->
                call.requireRole(Role.ADMIN, Role.DRIVER, Role.MEMBER)
                
                val carId = try {
                    Uuid.parse(resource.parent.parent.id)
                } catch (_: IllegalArgumentException) {
                    return@get call.respondError(
                        HttpStatusCode.BadRequest,
                        "Invalid carId format",
                        "INVALID_CAR_ID",
                        call.callId
                    )
                }
                
                val availabilityId = try {
                    Uuid.parse(resource.availabilityId)
                } catch (_: IllegalArgumentException) {
                    return@get call.respondError(
                        HttpStatusCode.BadRequest,
                        "Invalid availabilityId format",
                        "INVALID_AVAILABILITY_ID",
                        call.callId
                    )
                }

                val availability = availabilityService.getById(availabilityId)
                    ?: throw ApiException(HttpStatusCode.NotFound, message = "Availability window not found")

                if (availability.carId != carId) {
                    throw ApiException(
                        HttpStatusCode.NotFound,
                        message = "Availability does not belong to this car"
                    )
                }

                call.respondSuccess(availability.toAvailabilityDto())
            }

            // POST /api/v1/cars/{id}/availability - Add availability window for a car
            post<ApiV1.Cars.Id.Availability> { resource ->
                val principal = call.requireRole(Role.ADMIN, Role.DRIVER)
                val userId = Uuid.parse(principal.payload.getClaim("userId").asString())
                val role = Role.valueOf(principal.payload.getClaim("role").asString())

                val carId = try {
                    Uuid.parse(resource.parent.id)
                } catch (_: IllegalArgumentException) {
                    return@post call.respondError(
                        HttpStatusCode.BadRequest,
                        "Invalid carId format",
                        "INVALID_CAR_ID",
                        call.callId
                    )
                }
                
                val car = carService.getById(carId)
                    ?: throw ApiException(HttpStatusCode.NotFound, message = "Car not found")

                // Ownership check
                verifyOwnership(role, userId, car.ownerId, "car")

                val req = call.requireBodyOrFail<CreateCarAvailabilityRequestDto>()
                val created = availabilityService.create(req.toAvailabilityEntity(carId))
                call.respondCreated(created.toAvailabilityDto())
            }

            // PUT /api/v1/cars/{id}/availability/{availabilityId} - Replace availability window
            put<ApiV1.Cars.Id.Availability.AvailabilityId> { resource ->
                val principal = call.requireRole(Role.ADMIN, Role.DRIVER)
                val userId = Uuid.parse(principal.payload.getClaim("userId").asString())
                val role = Role.valueOf(principal.payload.getClaim("role").asString())

                val carId = try {
                    Uuid.parse(resource.parent.parent.id)
                } catch (_: IllegalArgumentException) {
                    return@put call.respondError(
                        HttpStatusCode.BadRequest,
                        "Invalid carId format",
                        "INVALID_CAR_ID",
                        call.callId
                    )
                }
                
                val availabilityId = try {
                    Uuid.parse(resource.availabilityId)
                } catch (_: IllegalArgumentException) {
                    return@put call.respondError(
                        HttpStatusCode.BadRequest,
                        "Invalid availabilityId format",
                        "INVALID_AVAILABILITY_ID",
                        call.callId
                    )
                }

                val availability = availabilityService.getById(availabilityId)
                    ?: throw ApiException(HttpStatusCode.NotFound, message = "Availability window not found")

                if (availability.carId != carId) {
                    throw ApiException(
                        HttpStatusCode.NotFound,
                        message = "Availability does not belong to this car"
                    )
                }

                val car = carService.getById(carId)
                    ?: throw ApiException(HttpStatusCode.NotFound, message = "Car not found")

                // Ownership check
                verifyOwnership(role, userId, car.ownerId, "car")

                val req = call.requireBodyOrFail<UpdateCarAvailabilityRequestDto>()
                val updated =
                    availabilityService.update(availabilityId, req.toAvailabilityEntity(availabilityId, carId))
                        ?: throw ApiException(
                            HttpStatusCode.InternalServerError,
                            message = "Failed to update availability window"
                        )

                call.respondSuccess(updated.toAvailabilityDto())
            }

            // PATCH /api/v1/cars/{id}/availability/{availabilityId} - Partially update availability window
            patch<ApiV1.Cars.Id.Availability.AvailabilityId> { resource ->
                val principal = call.requireRole(Role.ADMIN, Role.DRIVER)
                val userId = Uuid.parse(principal.payload.getClaim("userId").asString())
                val role = Role.valueOf(principal.payload.getClaim("role").asString())

                val carId = try {
                    Uuid.parse(resource.parent.parent.id)
                } catch (_: IllegalArgumentException) {
                    return@patch call.respondError(
                        HttpStatusCode.BadRequest,
                        "Invalid carId format",
                        "INVALID_CAR_ID",
                        call.callId
                    )
                }
                
                val availabilityId = try {
                    Uuid.parse(resource.availabilityId)
                } catch (_: IllegalArgumentException) {
                    return@patch call.respondError(
                        HttpStatusCode.BadRequest,
                        "Invalid availabilityId format",
                        "INVALID_AVAILABILITY_ID",
                        call.callId
                    )
                }

                val availability = availabilityService.getById(availabilityId)
                    ?: throw ApiException(HttpStatusCode.NotFound, message = "Availability window not found")

                if (availability.carId != carId) {
                    throw ApiException(
                        HttpStatusCode.NotFound,
                        message = "Availability does not belong to this car"
                    )
                }

                val car = carService.getById(carId)
                    ?: throw ApiException(HttpStatusCode.NotFound, message = "Car not found")

                // Ownership check
                verifyOwnership(role, userId, car.ownerId, "car")

                val req = call.requireBodyOrFail<PatchCarAvailabilityRequestDto>()
                val patched = req.applyAvailabilityPatch(availability)
                val saved = availabilityService.update(availabilityId, patched)
                    ?: throw ApiException(
                        HttpStatusCode.InternalServerError,
                        message = "Failed to update availability window"
                    )

                call.respondSuccess(saved.toAvailabilityDto())
            }

            // DELETE /api/v1/cars/{id}/availability/{availabilityId} - Delete availability window
            delete<ApiV1.Cars.Id.Availability.AvailabilityId> { resource ->
                val principal = call.requireRole(Role.ADMIN, Role.DRIVER)
                val userId = Uuid.parse(principal.payload.getClaim("userId").asString())
                val role = Role.valueOf(principal.payload.getClaim("role").asString())

                val carId = try {
                    Uuid.parse(resource.parent.parent.id)
                } catch (_: IllegalArgumentException) {
                    return@delete call.respondError(
                        HttpStatusCode.BadRequest,
                        "Invalid carId format",
                        "INVALID_CAR_ID",
                        call.callId
                    )
                }
                
                val availabilityId = try {
                    Uuid.parse(resource.availabilityId)
                } catch (_: IllegalArgumentException) {
                    return@delete call.respondError(
                        HttpStatusCode.BadRequest,
                        "Invalid availabilityId format",
                        "INVALID_AVAILABILITY_ID",
                        call.callId
                    )
                }

                val availability = availabilityService.getById(availabilityId)
                    ?: throw ApiException(HttpStatusCode.NotFound, message = "Availability window not found")

                if (availability.carId != carId) {
                    throw ApiException(
                        HttpStatusCode.NotFound,
                        message = "Availability does not belong to this car"
                    )
                }

                val car = carService.getById(carId)
                    ?: throw ApiException(HttpStatusCode.NotFound, message = "Car not found")

                // Ownership check
                verifyOwnership(role, userId, car.ownerId, "car")

                if (!availabilityService.delete(availabilityId)) {
                    throw ApiException(
                        HttpStatusCode.InternalServerError,
                        message = "Failed to delete availability window"
                    )
                }

                call.respondSuccess(Unit, HttpStatusCode.NoContent)
            }
        }
    }
}
