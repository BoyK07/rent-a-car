package dev.koenv.rentmycar.server.routes.api.v1.cars

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import dev.koenv.rentmycar.server.domain.service.CarService
import dev.koenv.rentmycar.server.domain.service.SearchService
import dev.koenv.rentmycar.server.mappers.car.applyPatch
import dev.koenv.rentmycar.server.mappers.car.toDto
import dev.koenv.rentmycar.server.mappers.car.toEntity
import dev.koenv.rentmycar.server.routes.RouteRegistrar
import dev.koenv.rentmycar.server.util.*
import dev.koenv.rentmycar.shared.domain.enums.CarCategory
import dev.koenv.rentmycar.shared.domain.enums.FuelType
import dev.koenv.rentmycar.shared.domain.enums.Role
import dev.koenv.rentmycar.shared.dto.car.CreateCarRequestDto
import dev.koenv.rentmycar.shared.dto.car.PatchCarRequestDto
import dev.koenv.rentmycar.shared.dto.car.UpdateCarRequestDto
import dev.koenv.rentmycar.shared.http.ApiException
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import kotlin.uuid.Uuid

object CarRoutes : RouteRegistrar {
    override fun Route.register() {
        val carService by inject<CarService>()
        val searchService by inject<SearchService>()

        route("/cars") {

            // Public list/search endpoint with optional location-based search
            get {
                // Location-based search parameters
                val latitude = call.requireDoubleParamOrNull("latitude")
                val longitude = call.requireDoubleParamOrNull("longitude")
                val maxDistance = call.requireDoubleParamOrNull("maxDistance")
                val minPrice = call.requireBigDecimalParamOrNull("minPrice")
                val maxPrice = call.requireBigDecimalParamOrNull("maxPrice")
                val brand = call.requireStringParamOrNull("brand")
                val page = call.requireIntParamOrNull("page")
                val limit = call.requireIntParamOrNull("limit")

                // Basic filter parameters (existing)
                val ownerId =
                    call.request.queryParameters["ownerId"]?.let { runCatching { Uuid.parse(it) }.getOrNull() }
                val category =
                    call.request.queryParameters["category"]?.let { runCatching { CarCategory.valueOf(it.uppercase()) }.getOrNull() }
                val fuelType =
                    call.request.queryParameters["fuelType"]?.let { runCatching { FuelType.valueOf(it.uppercase()) }.getOrNull() }
                val isActive = call.request.queryParameters["isActive"]?.toBooleanStrictOrNull()

                // If location parameters are provided, use search service with pagination
                if (latitude != null || longitude != null || maxDistance != null || minPrice != null || maxPrice != null || brand != null || page != null || limit != null) {
                    // Validate parameters
                    if (page != null && page < 1) {
                        throw ApiException(HttpStatusCode.BadRequest, message = "Page must be at least 1")
                    }
                    if (limit != null && (limit < 1 || limit > 100)) {
                        throw ApiException(HttpStatusCode.BadRequest, message = "Limit must be between 1 and 100")
                    }
                    if (latitude != null && (latitude < -90 || latitude > 90)) {
                        throw ApiException(HttpStatusCode.BadRequest, message = "Latitude must be between -90 and 90")
                    }
                    if (longitude != null && (longitude < -180 || longitude > 180)) {
                        throw ApiException(
                            HttpStatusCode.BadRequest,
                            message = "Longitude must be between -180 and 180"
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
                        call.respond(result)
                    } catch (e: IllegalArgumentException) {
                        throw ApiException(
                            HttpStatusCode.BadRequest,
                            message = e.message ?: "Invalid search parameters"
                        )
                    }
                } else {
                    // Use basic filtering (existing behavior)
                    val maxRate =
                        call.request.queryParameters["maxRate"]?.let { runCatching { BigDecimal.parseString(it) }.getOrNull() }
                    val items = carService.listFiltered(ownerId, category, fuelType, isActive, maxRate)
                    call.respond(items.map { it.toDto() })
                }
            }

            // Public single car endpoint
            get("/{id}") {
                val id = call.requireUuidParamOrFail("id")
                val car = carService.getById(id)
                if (car == null) {
                    throw ApiException(HttpStatusCode.NotFound, message = "Car not found")
                }
                call.respond(car.toDto())
            }

            authenticate("auth-jwt") {

                post {
                    val principal = call.requireRole(Role.ADMIN, Role.DRIVER)
                    val userId = Uuid.parse(principal.payload.getClaim("userId").asString())
                    val req = call.requireBodyOrFail<CreateCarRequestDto>()
                    val created = carService.create(req.toEntity(userId))
                    call.respond(HttpStatusCode.Created, created.toDto())
                }

                put("/{id}") {
                    val principal = call.requireRole(Role.ADMIN, Role.DRIVER)
                    val userId = Uuid.parse(principal.payload.getClaim("userId").asString())
                    val role = Role.valueOf(principal.payload.getClaim("role").asString())
                    val id = call.requireUuidParamOrFail("id")
                    val existing = carService.getById(id)

                    if (existing == null) {
                        throw ApiException(HttpStatusCode.NotFound, message = "Car not found")
                    }

                    // Owner check: only admin or owner can modify
                    verifyOwnership(role, userId, existing.ownerId, "car")

                    val req = call.requireBodyOrFail<UpdateCarRequestDto>()
                    val updated = carService.update(id, req.toEntity(id, existing.ownerId))
                    if (updated == null) call.respond(HttpStatusCode.NotFound) else call.respond(updated.toDto())
                }

                patch("/{id}") {
                    val principal = call.requireRole(Role.ADMIN, Role.DRIVER)
                    val userId = Uuid.parse(principal.payload.getClaim("userId").asString())
                    val role = Role.valueOf(principal.payload.getClaim("role").asString())
                    val id = call.requireUuidParamOrFail("id")
                    val existing = carService.getById(id)

                    if (existing == null) {
                        throw ApiException(HttpStatusCode.NotFound, message = "Car not found")
                    }

                    // Owner check
                    verifyOwnership(role, userId, existing.ownerId, "car")

                    val req = call.requireBodyOrFail<PatchCarRequestDto>()
                    val patched = req.applyPatch(existing)
                    val saved = carService.update(id, patched)
                    if (saved == null) call.respond(HttpStatusCode.NotFound) else call.respond(saved.toDto())
                }

                delete("/{id}") {
                    val principal = call.requireRole(Role.ADMIN, Role.DRIVER)
                    val userId = Uuid.parse(principal.payload.getClaim("userId").asString())
                    val role = Role.valueOf(principal.payload.getClaim("role").asString())
                    val id = call.requireUuidParamOrFail("id")
                    val existing = carService.getById(id)

                    if (existing == null) {
                        throw ApiException(HttpStatusCode.NotFound, message = "Car not found")
                    }

                    // Only admin or owner may delete
                    verifyOwnership(role, userId, existing.ownerId, "car")

                    if (carService.delete(id)) {
                        call.respond(HttpStatusCode.NoContent)
                    } else {
                        call.respond(HttpStatusCode.NotFound)
                    }
                }
            }
        }
    }
}
