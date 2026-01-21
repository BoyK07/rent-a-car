package dev.koenv.rentmycar.server.routes.api.v1.cars

/**
 * Car management API routes.
 * 
 * Endpoints:
 * - POST /api/v1/cars - Create new car listing (authenticated)
 * - GET /api/v1/cars/{id} - Get car details (public)
 * - PUT /api/v1/cars/{id} - Update car (owner or admin only)
 * - PATCH /api/v1/cars/{id} - Partial update car (owner or admin only)
 * - DELETE /api/v1/cars/{id} - Delete car (owner or admin only)
 * - GET /api/v1/cars - Search/list cars with filters (public)
 */

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import dev.koenv.rentmycar.server.domain.service.CarService
import dev.koenv.rentmycar.server.domain.service.SearchService
import dev.koenv.rentmycar.server.mappers.car.toDto
import dev.koenv.rentmycar.server.routes.RouteRegistrar
import dev.koenv.rentmycar.server.util.*
import dev.koenv.rentmycar.shared.domain.enums.CarCategory
import dev.koenv.rentmycar.shared.domain.enums.FuelType
import dev.koenv.rentmycar.shared.domain.enums.Role
import dev.koenv.rentmycar.shared.dto.car.CreateCarRequestDto
import dev.koenv.rentmycar.shared.dto.car.PatchCarRequestDto
import dev.koenv.rentmycar.shared.dto.car.UpdateCarRequestDto
import dev.koenv.rentmycar.shared.resources.ApiV1
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.resources.put
import io.ktor.server.resources.patch
import io.ktor.server.resources.delete
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import kotlin.uuid.Uuid

object CarRoutes : RouteRegistrar {
    override fun Route.register() {
        val carService by inject<CarService>()
        val searchService by inject<SearchService>()

        // Public list/search endpoint with optional location-based search
        get<ApiV1.Cars> { resource ->
            // Extract parameters from resource
            val latitude = resource.latitude
            val longitude = resource.longitude
            val maxDistance = resource.maxDistance
            val minPrice = resource.minPrice?.let { BigDecimal.parseString(it) }
            val maxPrice = resource.maxPrice?.let { BigDecimal.parseString(it) }
            val brand = resource.brand
            val page = resource.page
            val limit = resource.limit

            // Basic filter parameters
            val ownerId = resource.ownerId?.let { runCatching { Uuid.parse(it) }.getOrNull() }
            val category = resource.category?.let { runCatching { CarCategory.valueOf(it.uppercase()) }.getOrNull() }
            val fuelType = resource.fuelType?.let { runCatching { FuelType.valueOf(it.uppercase()) }.getOrNull() }
            val isActive = resource.isActive

            // If location parameters are provided, use search service with pagination
            if (latitude != null || longitude != null || maxDistance != null || minPrice != null || maxPrice != null || brand != null || page != null || limit != null) {
                // Validate parameters
                if (page != null && page < 1) {
                    return@get call.respondError(
                        HttpStatusCode.BadRequest,
                        "Page must be at least 1",
                        "INVALID_PAGE",
                        call.callId
                    )
                }
                if (limit != null && (limit < 1 || limit > 100)) {
                    return@get call.respondError(
                        HttpStatusCode.BadRequest,
                        "Limit must be between 1 and 100",
                        "INVALID_LIMIT",
                        call.callId
                    )
                }
                if (latitude != null && (latitude < -90 || latitude > 90)) {
                    return@get call.respondError(
                        HttpStatusCode.BadRequest,
                        "Latitude must be between -90 and 90",
                        "INVALID_LATITUDE",
                        call.callId
                    )
                }
                if (longitude != null && (longitude < -180 || longitude > 180)) {
                    return@get call.respondError(
                        HttpStatusCode.BadRequest,
                        "Longitude must be between -180 and 180",
                        "INVALID_LONGITUDE",
                        call.callId
                    )
                }

                try {
                    val result = searchService.searchCars(
                        latitude = latitude,
                        longitude = longitude,
                        maxDistance = maxDistance,
                        minPrice = minPrice,
                        maxPrice = maxPrice,
                        category = category,
                        fuelType = fuelType,
                        brand = brand,
                        page = page ?: 1,
                        limit = limit ?: 20
                    )
                    call.respondSuccess(result)
                } catch (e: IllegalArgumentException) {
                    call.respondError(
                        HttpStatusCode.BadRequest,
                        e.message ?: "Invalid search parameters",
                        "INVALID_SEARCH_PARAMETERS",
                        call.callId
                    )
                }
            } else {
                // Use basic filtering (existing behavior)
                val maxRate = resource.maxRate?.let { BigDecimal.parseString(it) }
                val items = carService.listFiltered(ownerId, category, fuelType, isActive, maxRate)
                call.respondSuccess(items.map { it.toDto() })
            }
        }

        // Public single car endpoint
        get<ApiV1.Cars.Id> { resource ->
            val id = Uuid.parse(resource.id)
            val car = carService.getById(id)
            if (car == null) {
                return@get call.respondError(
                    HttpStatusCode.NotFound,
                    "Car not found",
                    "CAR_NOT_FOUND",
                    call.callId
                )
            }
            call.respondSuccess(car.toDto())
        }

        authenticate("auth-jwt") {

            post<ApiV1.Cars> { resource ->
                val principal = call.requireRole(Role.ADMIN, Role.DRIVER)
                val userId = Uuid.parse(principal.payload.getClaim("userId").asString())
                val req = call.requireBodyOrFail<CreateCarRequestDto>()
                try {
                    val created = carService.createFromRequest(req, userId)
                    call.respondCreated(created.toDto())
                } catch (e: IllegalArgumentException) {
                    call.respondError(
                        HttpStatusCode.BadRequest,
                        e.message ?: "Invalid address",
                        "INVALID_ADDRESS",
                        call.callId
                    )
                } catch (e: IllegalStateException) {
                    call.respondError(
                        HttpStatusCode.InternalServerError,
                        e.message ?: "Geocoding service unavailable",
                        "GEOCODING_UNAVAILABLE",
                        call.callId
                    )
                }
            }

            put<ApiV1.Cars.Id> { resource ->
                val principal = call.requireRole(Role.ADMIN, Role.DRIVER)
                val userId = Uuid.parse(principal.payload.getClaim("userId").asString())
                val role = Role.valueOf(principal.payload.getClaim("role").asString())
                val id = Uuid.parse(resource.id)
                val existing = carService.getById(id)

                if (existing == null) {
                    return@put call.respondError(
                        HttpStatusCode.NotFound,
                        "Car not found",
                        "CAR_NOT_FOUND",
                        call.callId
                    )
                }

                // Owner check: only admin or owner can modify
                verifyOwnership(role, userId, existing.ownerId, "car")

                val req = call.requireBodyOrFail<UpdateCarRequestDto>()
                try {
                    val updated = carService.updateFromRequest(id, existing.ownerId, req)
                    if (updated == null) {
                        call.respondError(HttpStatusCode.NotFound, "Car not found", "CAR_NOT_FOUND", call.callId)
                    } else {
                        call.respondSuccess(updated.toDto())
                    }
                } catch (e: IllegalArgumentException) {
                    call.respondError(
                        HttpStatusCode.BadRequest,
                        e.message ?: "Invalid address",
                        "INVALID_ADDRESS",
                        call.callId
                    )
                } catch (e: IllegalStateException) {
                    call.respondError(
                        HttpStatusCode.InternalServerError,
                        e.message ?: "Geocoding service unavailable",
                        "GEOCODING_UNAVAILABLE",
                        call.callId
                    )
                }
            }

            patch<ApiV1.Cars.Id> { resource ->
                val principal = call.requireRole(Role.ADMIN, Role.DRIVER)
                val userId = Uuid.parse(principal.payload.getClaim("userId").asString())
                val role = Role.valueOf(principal.payload.getClaim("role").asString())
                val id = Uuid.parse(resource.id)
                val existing = carService.getById(id)

                if (existing == null) {
                    return@patch call.respondError(
                        HttpStatusCode.NotFound,
                        "Car not found",
                        "CAR_NOT_FOUND",
                        call.callId
                    )
                }

                // Owner check
                verifyOwnership(role, userId, existing.ownerId, "car")

                val req = call.requireBodyOrFail<PatchCarRequestDto>()
                try {
                    val saved = carService.patchFromRequest(id, existing, req)
                    if (saved == null) {
                        call.respondError(HttpStatusCode.NotFound, "Car not found", "CAR_NOT_FOUND", call.callId)
                    } else {
                        call.respondSuccess(saved.toDto())
                    }
                } catch (e: IllegalArgumentException) {
                    call.respondError(
                        HttpStatusCode.BadRequest,
                        e.message ?: "Invalid address",
                        "INVALID_ADDRESS",
                        call.callId
                    )
                } catch (e: IllegalStateException) {
                    call.respondError(
                        HttpStatusCode.InternalServerError,
                        e.message ?: "Geocoding service unavailable",
                        "GEOCODING_UNAVAILABLE",
                        call.callId
                    )
                }
            }

            delete<ApiV1.Cars.Id> { resource ->
                val principal = call.requireRole(Role.ADMIN, Role.DRIVER)
                val userId = Uuid.parse(principal.payload.getClaim("userId").asString())
                val role = Role.valueOf(principal.payload.getClaim("role").asString())
                val id = Uuid.parse(resource.id)
                val existing = carService.getById(id)

                if (existing == null) {
                    return@delete call.respondError(
                        HttpStatusCode.NotFound,
                        "Car not found",
                        "CAR_NOT_FOUND",
                        call.callId
                    )
                }

                // Only admin or owner may delete
                verifyOwnership(role, userId, existing.ownerId, "car")

                if (carService.delete(id)) {
                    call.respondSuccess(Unit, HttpStatusCode.NoContent)
                } else {
                    call.respondError(HttpStatusCode.NotFound, "Car not found")
                }
            }
        }
    }
}
