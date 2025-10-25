package dev.koenv.rentmycar.routes.api.v1.search

import dev.koenv.rentmycar.domain.enums.CarCategory
import dev.koenv.rentmycar.domain.enums.FuelType
import dev.koenv.rentmycar.domain.service.SearchService
import dev.koenv.rentmycar.dto.search.NearbySearchRequestDto
import dev.koenv.rentmycar.routes.RouteRegistrar
import dev.koenv.rentmycar.shared.http.ApiException
import dev.koenv.rentmycar.shared.util.requireBigDecimalParamOrNull
import dev.koenv.rentmycar.shared.util.requireDoubleParamOrNull
import dev.koenv.rentmycar.shared.util.requireIntParamOrNull
import dev.koenv.rentmycar.shared.util.requireStringParamOrNull
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

object SearchRoutes : RouteRegistrar {
    override fun Route.register() {
        val searchService by inject<SearchService>()

        route("/search") {
            get("/cars") {
                try {
                    // Get all query parameters
                    val latitude = call.requireDoubleParamOrNull("latitude")
                    val longitude = call.requireDoubleParamOrNull("longitude")
                    val maxDistance = call.requireDoubleParamOrNull("maxDistance")
                    val minPrice = call.requireBigDecimalParamOrNull("minPrice")
                    val maxPrice = call.requireBigDecimalParamOrNull("maxPrice")
                    val category = call.requireStringParamOrNull("category")?.let {
                        try {
                            CarCategory.valueOf(it.uppercase())
                        } catch (_: IllegalArgumentException) {
                            null
                        }
                    }
                    val fuelType = call.requireStringParamOrNull("fuelType")?.let {
                        try {
                            FuelType.valueOf(it.uppercase())
                        } catch (_: IllegalArgumentException) {
                            null
                        }
                    }
                    val brand = call.requireStringParamOrNull("brand")
                    val page = call.requireIntParamOrNull("page") ?: 1
                    val limit = call.requireIntParamOrNull("limit") ?: 20

                    // validate parameters
                    if (page < 1) {
                        throw ApiException(
                            HttpStatusCode.BadRequest,
                            code = "SEARCH_ERROR",
                            message = "Page must be at least 1!"
                        )
                    }
                    if (limit < 1 || limit > 100) {
                        throw ApiException(
                            HttpStatusCode.BadRequest,
                            code = "SEARCH_ERROR",
                            message = "Limit must be between 1 and 100!"
                        )
                    }
                    if (latitude != null && longitude != null) {
                        if (latitude < -90 || latitude > 90) {
                            throw ApiException(
                                HttpStatusCode.BadRequest,
                                code = "SEARCH_ERROR",
                                message = "Latitude must be between -90 and 90!"
                            )
                        }
                        if (longitude < -180 || longitude > 180) {
                            throw ApiException(
                                HttpStatusCode.BadRequest,
                                code = "SEARCH_ERROR",
                                message = "Longitude must be between -180 and 90!"
                            )
                        }
                    }

                    val result = searchService.searchCars(
                        latitude = latitude,
                        longitude = longitude,
                        maxDistance = maxDistance,
                        minPrice = minPrice,
                        maxPrice = maxPrice,
                        category = category,
                        fuelType = fuelType,
                        brand = brand,
                        page = page,
                        limit = limit
                    )

                    call.respond(HttpStatusCode.OK, result)

                } catch (e: IllegalArgumentException) {
                    throw ApiException(
                        HttpStatusCode.BadRequest,
                        code = "SEARCH_ERROR",
                        message = e.message.toString(),
                    )
                }
            }

            get("/cars/nearby") {
                val latitude = call.requireDoubleParamOrNull("latitude")
                val longitude = call.requireDoubleParamOrNull("longitude")
                val radius = call.requireDoubleParamOrNull("radius") ?: 10.0
                val page = call.requireIntParamOrNull("page") ?: 1
                val limit = call.requireIntParamOrNull("limit") ?: 20

                // Validate required parameters
                if (latitude == null || longitude == null) {
                    throw ApiException(
                        HttpStatusCode.BadRequest,
                        code = "SEARCH_ERROR",
                        message = "Latitude and longitude are required",
                    )
                }

                if (latitude < -90 || latitude > 90) {
                    throw ApiException(
                        HttpStatusCode.BadRequest,
                        code = "SEARCH_ERROR",
                        message = "Latitude must be between -90 and 90"
                    )
                }
                if (longitude < -180 || longitude > 180) {
                    throw ApiException(
                        HttpStatusCode.BadRequest,
                        code = "SEARCH_ERROR",
                        message = "Longitude must be between -180 and 90"
                    )
                }
                if (radius <= 0 || radius > 100) {
                    throw ApiException(
                        HttpStatusCode.BadRequest,
                        code = "SEARCH_ERROR",
                        message = "Radius must be between 0 and 100"
                    )
                }
                if (limit < 1 || limit > 100) {
                    throw ApiException(
                        HttpStatusCode.BadRequest,
                        code = "SEARCH_ERROR",
                        message = "Limit must be between -1 and 100"
                    )
                }

                val request = NearbySearchRequestDto(
                    latitude = latitude,
                    longitude = longitude,
                    radius = radius,
                    page = page,
                    limit = limit
                )

                val result = searchService.searchNearbyCars(request)
                call.respond(HttpStatusCode.OK, result)
            }
        }
    }
}
