package dev.koenv.rentmycar.shared.api

import dev.koenv.rentmycar.shared.dto.reservation.*
import dev.koenv.rentmycar.shared.http.ApiResponse
import dev.koenv.rentmycar.shared.resources.ApiV1
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlin.uuid.Uuid

/**
 * API client for reservation endpoints.
 */
class ReservationApi(
    private val httpClient: HttpClient
) {
    /**
     * Get price quote for a reservation before booking.
     */
    suspend fun getQuote(request: ReservationQuoteRequestDto): Result<ReservationQuoteResponseDto> {
        return try {
            val response = httpClient.post(ApiV1.Reservations.Quote()) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val apiResponse = response.body<ApiResponse<ReservationQuoteResponseDto>>()
            if (apiResponse.success && apiResponse.data != null) {
                Result.success(apiResponse.data)
            } else {
                Result.failure(Exception(apiResponse.message ?: "Failed to get quote"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get all reservations with optional filters.
     */
    suspend fun getReservations(
        renterId: Uuid? = null,
        carId: Uuid? = null,
        status: String? = null,
        start: String? = null,
        end: String? = null
    ): Result<List<ReservationDto>> {
        return try {
            val response = httpClient.get(ApiV1.Reservations(
                renterId = renterId?.toString(),
                carId = carId?.toString(),
                status = status,
                start = start,
                end = end
            ))
            val apiResponse = response.body<ApiResponse<List<ReservationDto>>>()
            if (apiResponse.success && apiResponse.data != null) {
                Result.success(apiResponse.data)
            } else {
                Result.failure(Exception(apiResponse.message ?: "Failed to fetch reservations"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get active reservations.
     */
    suspend fun getActiveReservations(): Result<List<ReservationDto>> {
        return try {
            val response = httpClient.get(ApiV1.Reservations.Active())
            val apiResponse = response.body<ApiResponse<List<ReservationDto>>>()
            if (apiResponse.success && apiResponse.data != null) {
                Result.success(apiResponse.data)
            } else {
                Result.failure(Exception(apiResponse.message ?: "Failed to fetch active reservations"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get reservations for user's owned cars.
     * This returns reservations where the current user owns the car being reserved.
     */
    suspend fun getMyCarReservations(): Result<List<ReservationDto>> {
        return try {
            val response = httpClient.get(ApiV1.Reservations.MyCars())
            val apiResponse = response.body<ApiResponse<List<ReservationDto>>>()
            if (apiResponse.success && apiResponse.data != null) {
                Result.success(apiResponse.data)
            } else {
                Result.failure(Exception(apiResponse.message ?: "Failed to fetch car reservations"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get a single reservation by ID.
     */
    suspend fun getReservation(id: Uuid): Result<ReservationDto> {
        return try {
            val response = httpClient.get(ApiV1.Reservations.Id(id = id.toString()))
            val apiResponse = response.body<ApiResponse<ReservationDto>>()
            if (apiResponse.success && apiResponse.data != null) {
                Result.success(apiResponse.data)
            } else {
                Result.failure(Exception(apiResponse.message ?: "Failed to fetch reservation"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Create a new reservation.
     */
    suspend fun createReservation(request: CreateReservationRequestDto): Result<ReservationDto> {
        return try {
            val response = httpClient.post(ApiV1.Reservations()) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val apiResponse = response.body<ApiResponse<ReservationDto>>()
            if (apiResponse.success && apiResponse.data != null) {
                Result.success(apiResponse.data)
            } else {
                Result.failure(Exception(apiResponse.message ?: "Failed to create reservation"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update an existing reservation.
     */
    suspend fun updateReservation(id: Uuid, request: UpdateReservationRequestDto): Result<ReservationDto> {
        return try {
            val response = httpClient.put(ApiV1.Reservations.Id(id = id.toString())) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val apiResponse = response.body<ApiResponse<ReservationDto>>()
            if (apiResponse.success && apiResponse.data != null) {
                Result.success(apiResponse.data)
            } else {
                Result.failure(Exception(apiResponse.message ?: "Failed to update reservation"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Partially update an existing reservation.
     */
    suspend fun patchReservation(id: Uuid, request: PatchReservationRequestDto): Result<ReservationDto> {
        return try {
            val response = httpClient.patch(ApiV1.Reservations.Id(id = id.toString())) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val apiResponse = response.body<ApiResponse<ReservationDto>>()
            if (apiResponse.success && apiResponse.data != null) {
                Result.success(apiResponse.data)
            } else {
                Result.failure(Exception(apiResponse.message ?: "Failed to patch reservation"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Delete a reservation.
     */
    suspend fun deleteReservation(id: Uuid): Result<Unit> {
        return try {
            val response = httpClient.delete(ApiV1.Reservations.Id(id = id.toString()))
            val apiResponse = response.body<ApiResponse<Unit>>()
            if (apiResponse.success) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(apiResponse.message ?: "Failed to delete reservation"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Cancel a reservation.
     */
    suspend fun cancelReservation(id: Uuid): Result<Unit> {
        return try {
            val response = httpClient.post(ApiV1.Reservations.Id.Cancel(parent = ApiV1.Reservations.Id(id = id.toString())))
            val apiResponse = response.body<ApiResponse<Unit>>()
            if (apiResponse.success) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(apiResponse.message ?: "Failed to cancel reservation"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Confirm a reservation (owner only).
     */
    suspend fun confirmReservation(id: Uuid): Result<ReservationDto> {
        return try {
            val response = httpClient.post(ApiV1.Reservations.Id.Confirm(parent = ApiV1.Reservations.Id(id = id.toString())))
            val apiResponse = response.body<ApiResponse<ReservationDto>>()
            if (apiResponse.success && apiResponse.data != null) {
                Result.success(apiResponse.data)
            } else {
                Result.failure(Exception(apiResponse.message ?: "Failed to confirm reservation"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Complete a reservation.
     */
    suspend fun completeReservation(id: Uuid): Result<ReservationDto> {
        return try {
            val response = httpClient.post(ApiV1.Reservations.Id.Complete(parent = ApiV1.Reservations.Id(id = id.toString())))
            val apiResponse = response.body<ApiResponse<ReservationDto>>()
            if (apiResponse.success && apiResponse.data != null) {
                Result.success(apiResponse.data)
            } else {
                Result.failure(Exception(apiResponse.message ?: "Failed to complete reservation"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get driving sessions for a reservation.
     */
    suspend fun getDrivingSessions(reservationId: Uuid): Result<List<DrivingSessionDto>> {
        return try {
            val response = httpClient.get(ApiV1.Reservations.Id.DrivingSessions(parent = ApiV1.Reservations.Id(id = reservationId.toString())))
            val apiResponse = response.body<ApiResponse<List<DrivingSessionDto>>>()
            if (apiResponse.success && apiResponse.data != null) {
                Result.success(apiResponse.data)
            } else {
                Result.failure(Exception(apiResponse.message ?: "Failed to fetch driving sessions"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Create a driving session for a reservation.
     */
    suspend fun createDrivingSession(reservationId: Uuid, request: CreateDrivingSessionRequestDto): Result<DrivingSessionDto> {
        return try {
            val response = httpClient.post(ApiV1.Reservations.Id.DrivingSessions(parent = ApiV1.Reservations.Id(id = reservationId.toString()))) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val apiResponse = response.body<ApiResponse<DrivingSessionDto>>()
            if (apiResponse.success && apiResponse.data != null) {
                Result.success(apiResponse.data)
            } else {
                Result.failure(Exception(apiResponse.message ?: "Failed to create driving session"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
