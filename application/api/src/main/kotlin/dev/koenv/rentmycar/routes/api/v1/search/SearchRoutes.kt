package dev.koenv.rentmycar.routes.api.v1.search

import dev.koenv.rentmycar.domain.enums.CarCategory
import dev.koenv.rentmycar.domain.enums.FuelType
import dev.koenv.rentmycar.domain.service.SearchService
import dev.koenv.rentmycar.dto.search.NearbySearchRequestDto
import dev.koenv.rentmycar.routes.RouteRegistrar
import dev.koenv.rentmycar.shared.util.requireBigDecimalParamOrNull
import dev.koenv.rentmycar.shared.util.requireDoubleParamOrNull
import dev.koenv.rentmycar.shared.util.requireIntParamOrNull
import dev.koenv.rentmycar.shared.util.requireStringParamOrNull
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

object SearchRoutes : RouteRegistrar {
    override fun Route.register() {
        val searchService by inject<SearchService>()

        route("/search") {
            get("/cars") {
                try {
                    // Query parameters ophalen
                    val latitude = call.requireDoubleParamOrNull("latitude")
                    val longitude = call.requireDoubleParamOrNull("longitude")
                    val maxDistance = call.requireDoubleParamOrNull("maxDistance")
                    val minPrice = call.requireBigDecimalParamOrNull("minPrice")
                    val maxPrice = call.requireBigDecimalParamOrNull("maxPrice")
                    val category = call.requireStringParamOrNull("category")?.let { 
                        try { CarCategory.valueOf(it.uppercase()) } catch (e: IllegalArgumentException) { null }
                    }
                    val fuelType = call.requireStringParamOrNull("fuelType")?.let { 
                        try { FuelType.valueOf(it.uppercase()) } catch (e: IllegalArgumentException) { null }
                    }
                    val brand = call.requireStringParamOrNull("brand")
                    val page = call.requireIntParamOrNull("page") ?: 1
                    val limit = call.requireIntParamOrNull("limit") ?: 20

                    // Valideer parameters
                    if (page < 1) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Page moet minimaal 1 zijn"))
                        return@get
                    }
                    if (limit < 1 || limit > 100) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Limit moet tussen 1 en 100 zijn"))
                        return@get
                    }
                    if (latitude != null && longitude != null) {
                        if (latitude < -90 || latitude > 90) {
                            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Latitude moet tussen -90 en 90 zijn"))
                            return@get
                        }
                        if (longitude < -180 || longitude > 180) {
                            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Longitude moet tussen -180 en 180 zijn"))
                            return@get
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
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Er is een fout opgetreden bij het zoeken"))
                }
            }

            get("/cars/nearby") {
                try {
                    val latitude = call.requireDoubleParamOrNull("latitude")
                    val longitude = call.requireDoubleParamOrNull("longitude")
                    val radius = call.requireDoubleParamOrNull("radius") ?: 10.0
                    val limit = call.requireIntParamOrNull("limit") ?: 20

                    // Valideer verplichte parameters
                    if (latitude == null || longitude == null) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Latitude en longitude zijn verplicht"))
                        return@get
                    }

                    if (latitude < -90 || latitude > 90) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Latitude moet tussen -90 en 90 zijn"))
                        return@get
                    }
                    if (longitude < -180 || longitude > 180) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Longitude moet tussen -180 en 180 zijn"))
                        return@get
                    }
                    if (radius <= 0 || radius > 100) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Radius moet tussen 0 en 100 km zijn"))
                        return@get
                    }
                    if (limit < 1 || limit > 100) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Limit moet tussen 1 en 100 zijn"))
                        return@get
                    }

                    val request = NearbySearchRequestDto(
                        latitude = latitude,
                        longitude = longitude,
                        radius = radius,
                        limit = limit
                    )

                    val result = searchService.searchNearbyCars(request)
                    call.respond(HttpStatusCode.OK, result)

                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Er is een fout opgetreden bij het zoeken"))
                }
            }
        }
    }
}
