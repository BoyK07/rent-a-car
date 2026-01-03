package dev.koenv.rentmycar.shared.api

import dev.koenv.rentmycar.shared.dto.car.CarDto
import dev.koenv.rentmycar.shared.dto.search.SearchResultDto
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlin.uuid.Uuid

/**
 * API client for car endpoints.
 */
class CarsApi(
    private val httpClient: HttpClient
) {
    /**
     * Fetches a list of cars with optional search parameters.
     * Returns SearchResultDto when search parameters are provided, or List<CarDto> for basic filtering.
     */
    suspend fun getCars(
        page: Int? = null,
        limit: Int? = null,
        latitude: Double? = null,
        longitude: Double? = null,
        maxDistance: Double? = null,
        minPrice: String? = null,
        maxPrice: String? = null,
        brand: String? = null,
        category: String? = null,
        fuelType: String? = null,
        isActive: Boolean? = null
    ): Result<Any> {
        return try {
            val response = httpClient.get("/api/v1/cars") {
                page?.let { parameter("page", it) }
                limit?.let { parameter("limit", it) }
                latitude?.let { parameter("latitude", it) }
                longitude?.let { parameter("longitude", it) }
                maxDistance?.let { parameter("maxDistance", it) }
                minPrice?.let { parameter("minPrice", it) }
                maxPrice?.let { parameter("maxPrice", it) }
                brand?.let { parameter("brand", it) }
                category?.let { parameter("category", it) }
                fuelType?.let { parameter("fuelType", it) }
                isActive?.let { parameter("isActive", it) }
            }
            
            // Determine response type based on parameters
            if (page != null || limit != null || latitude != null || longitude != null || 
                maxDistance != null || minPrice != null || maxPrice != null || brand != null ||
                category != null || fuelType != null) {
                // Search endpoint returns SearchResultDto
                Result.success(response.body<SearchResultDto>())
            } else {
                // Basic filtering returns List<CarDto>
                Result.success(response.body<List<CarDto>>())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Fetches a single car by ID.
     */
    suspend fun getCar(id: Uuid): Result<CarDto> {
        return try {
            val response = httpClient.get("/api/v1/cars/$id")
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
