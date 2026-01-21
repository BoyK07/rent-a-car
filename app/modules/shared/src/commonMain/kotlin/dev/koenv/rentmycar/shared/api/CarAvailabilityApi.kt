package dev.koenv.rentmycar.shared.api

import dev.koenv.rentmycar.shared.dto.car.CarAvailabilityDto
import dev.koenv.rentmycar.shared.dto.car.CreateCarAvailabilityRequestDto
import dev.koenv.rentmycar.shared.dto.car.PatchCarAvailabilityRequestDto
import dev.koenv.rentmycar.shared.dto.car.UpdateCarAvailabilityRequestDto
import dev.koenv.rentmycar.shared.http.ApiResponse
import dev.koenv.rentmycar.shared.resources.ApiV1
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlin.uuid.Uuid

/**
 * API client for car availability endpoints.
 * NOTE: Backend implementation may be incomplete - verify endpoints before heavy use.
 */
class CarAvailabilityApi(
    private val httpClient: HttpClient
) {
    /**
     * Get availability windows for a specific car.
     */
    suspend fun getCarAvailability(carId: Uuid): Result<List<CarAvailabilityDto>> {
        return try {
            val response = httpClient.get(ApiV1.Cars.Id.Availability(
                parent = ApiV1.Cars.Id(id = carId.toString())
            ))
            val apiResponse = response.body<ApiResponse<List<CarAvailabilityDto>>>()
            if (apiResponse.success && apiResponse.data != null) {
                Result.success(apiResponse.data)
            } else {
                Result.failure(Exception(apiResponse.message ?: "Failed to fetch car availability"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get a single availability window by ID for a specific car.
     */
    suspend fun getCarAvailabilityById(carId: Uuid, availabilityId: Uuid): Result<CarAvailabilityDto> {
        return try {
            val response = httpClient.get(
                ApiV1.Cars.Id.Availability.AvailabilityId(
                    parent = ApiV1.Cars.Id.Availability(
                        parent = ApiV1.Cars.Id(id = carId.toString())
                    ),
                    availabilityId = availabilityId.toString()
                )
            )
            val apiResponse = response.body<ApiResponse<CarAvailabilityDto>>()
            if (apiResponse.success && apiResponse.data != null) {
                Result.success(apiResponse.data)
            } else {
                Result.failure(Exception(apiResponse.message ?: "Failed to fetch car availability"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Check availability for a specific car and time range.
     * Returns availability windows that match the criteria.
     */
    suspend fun checkAvailability(carId: Uuid, start: String, end: String): Result<List<CarAvailabilityDto>> {
        return try {
            val response = httpClient.get(ApiV1.Availability(
                carId = carId.toString(),
                start = start,
                end = end
            ))
            val apiResponse = response.body<ApiResponse<List<CarAvailabilityDto>>>()
            if (apiResponse.success && apiResponse.data != null) {
                Result.success(apiResponse.data)
            } else {
                Result.failure(Exception(apiResponse.message ?: "Failed to check availability"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Create a new availability window for a car.
     */
    suspend fun createCarAvailability(carId: Uuid, request: CreateCarAvailabilityRequestDto): Result<CarAvailabilityDto> {
        return try {
            val response = httpClient.post(
                ApiV1.Cars.Id.Availability(
                    parent = ApiV1.Cars.Id(id = carId.toString())
                )
            ) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val apiResponse = response.body<ApiResponse<CarAvailabilityDto>>()
            if (apiResponse.success && apiResponse.data != null) {
                Result.success(apiResponse.data)
            } else {
                Result.failure(Exception(apiResponse.message ?: "Failed to create car availability"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update an availability window for a car.
     */
    suspend fun updateCarAvailability(
        carId: Uuid,
        availabilityId: Uuid,
        request: UpdateCarAvailabilityRequestDto
    ): Result<CarAvailabilityDto> {
        return try {
            val response = httpClient.put(
                ApiV1.Cars.Id.Availability.AvailabilityId(
                    parent = ApiV1.Cars.Id.Availability(
                        parent = ApiV1.Cars.Id(id = carId.toString())
                    ),
                    availabilityId = availabilityId.toString()
                )
            ) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val apiResponse = response.body<ApiResponse<CarAvailabilityDto>>()
            if (apiResponse.success && apiResponse.data != null) {
                Result.success(apiResponse.data)
            } else {
                Result.failure(Exception(apiResponse.message ?: "Failed to update car availability"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Partially update an availability window for a car.
     */
    suspend fun patchCarAvailability(
        carId: Uuid,
        availabilityId: Uuid,
        request: PatchCarAvailabilityRequestDto
    ): Result<CarAvailabilityDto> {
        return try {
            val response = httpClient.patch(
                ApiV1.Cars.Id.Availability.AvailabilityId(
                    parent = ApiV1.Cars.Id.Availability(
                        parent = ApiV1.Cars.Id(id = carId.toString())
                    ),
                    availabilityId = availabilityId.toString()
                )
            ) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val apiResponse = response.body<ApiResponse<CarAvailabilityDto>>()
            if (apiResponse.success && apiResponse.data != null) {
                Result.success(apiResponse.data)
            } else {
                Result.failure(Exception(apiResponse.message ?: "Failed to patch car availability"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Delete an availability window for a car.
     */
    suspend fun deleteCarAvailability(carId: Uuid, availabilityId: Uuid): Result<Unit> {
        return try {
            val response = httpClient.delete(
                ApiV1.Cars.Id.Availability.AvailabilityId(
                    parent = ApiV1.Cars.Id.Availability(
                        parent = ApiV1.Cars.Id(id = carId.toString())
                    ),
                    availabilityId = availabilityId.toString()
                )
            )
            if (response.status.value in 200..299) {
                Result.success(Unit)
            } else {
                val apiResponse = response.body<ApiResponse<Any?>>()
                Result.failure(Exception(apiResponse.message ?: "Failed to delete car availability"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
