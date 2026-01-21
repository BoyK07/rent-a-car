package dev.koenv.rentmycar.shared.api

import dev.koenv.rentmycar.shared.dto.car.CarDto
import dev.koenv.rentmycar.shared.dto.car.CreateCarRequestDto
import dev.koenv.rentmycar.shared.dto.car.PatchCarRequestDto
import dev.koenv.rentmycar.shared.dto.car.UpdateCarRequestDto
import dev.koenv.rentmycar.shared.dto.search.SearchResultDto
import dev.koenv.rentmycar.shared.http.ApiResponse
import dev.koenv.rentmycar.shared.resources.ApiV1
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*
import io.ktor.http.*
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
        isActive: Boolean? = null,
        ownerId: String? = null
    ): Result<Any> {
        return try {
            val response = httpClient.get(ApiV1.Cars(
                page = page,
                limit = limit,
                latitude = latitude,
                longitude = longitude,
                maxDistance = maxDistance,
                minPrice = minPrice,
                maxPrice = maxPrice,
                brand = brand,
                category = category,
                fuelType = fuelType,
                isActive = isActive,
                ownerId = ownerId
            ))
            
            // Determine response type based on parameters
            if (page != null || limit != null || latitude != null || longitude != null || 
                maxDistance != null || minPrice != null || maxPrice != null || brand != null ||
                category != null || fuelType != null) {
                // Search endpoint returns SearchResultDto
                val apiResponse = response.body<ApiResponse<SearchResultDto>>()
                if (apiResponse.success && apiResponse.data != null) {
                    Result.success(apiResponse.data)
                } else {
                    Result.failure(Exception(apiResponse.message ?: "Failed to fetch cars"))
                }
            } else {
                // Basic filtering returns List<CarDto>
                val apiResponse = response.body<ApiResponse<List<CarDto>>>()
                if (apiResponse.success && apiResponse.data != null) {
                    Result.success(apiResponse.data)
                } else {
                    Result.failure(Exception(apiResponse.message ?: "Failed to fetch cars"))
                }
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
            val response = httpClient.get(ApiV1.Cars.Id(id = id.toString()))
            val apiResponse = response.body<ApiResponse<CarDto>>()
            if (apiResponse.success && apiResponse.data != null) {
                Result.success(apiResponse.data)
            } else {
                Result.failure(Exception(apiResponse.message ?: "Failed to fetch car"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Creates a new car (requires authentication).
     */
    suspend fun createCar(request: CreateCarRequestDto): Result<CarDto> {
        return try {
            val response = httpClient.post(ApiV1.Cars()) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val apiResponse = response.body<ApiResponse<CarDto>>()
            if (apiResponse.success && apiResponse.data != null) {
                Result.success(apiResponse.data)
            } else {
                Result.failure(Exception(apiResponse.message ?: "Failed to create car"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Updates an existing car (requires authentication).
     */
    suspend fun updateCar(id: Uuid, request: UpdateCarRequestDto): Result<CarDto> {
        return try {
            val response = httpClient.put(ApiV1.Cars.Id(id = id.toString())) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val apiResponse = response.body<ApiResponse<CarDto>>()
            if (apiResponse.success && apiResponse.data != null) {
                Result.success(apiResponse.data)
            } else {
                Result.failure(Exception(apiResponse.message ?: "Failed to update car"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Partially updates an existing car (requires authentication).
     */
    suspend fun patchCar(id: Uuid, request: PatchCarRequestDto): Result<CarDto> {
        return try {
            val response = httpClient.patch(ApiV1.Cars.Id(id = id.toString())) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val apiResponse = response.body<ApiResponse<CarDto>>()
            if (apiResponse.success && apiResponse.data != null) {
                Result.success(apiResponse.data)
            } else {
                Result.failure(Exception(apiResponse.message ?: "Failed to patch car"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Deletes a car by ID (requires authentication).
     */
    suspend fun deleteCar(id: Uuid): Result<Unit> {
        return try {
            val response = httpClient.delete(ApiV1.Cars.Id(id = id.toString()))
            if (response.status.value in 200..299) {
                Result.success(Unit)
            } else {
                val apiResponse = response.body<ApiResponse<Any?>>()
                Result.failure(Exception(apiResponse.message ?: "Failed to delete car"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
