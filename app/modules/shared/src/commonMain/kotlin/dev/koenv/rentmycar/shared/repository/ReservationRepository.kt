package dev.koenv.rentmycar.shared.repository

import dev.koenv.rentmycar.shared.api.ReservationApi
import dev.koenv.rentmycar.shared.db.dao.ReservationDao
import dev.koenv.rentmycar.shared.domain.enums.ReservationStatus
import dev.koenv.rentmycar.shared.dto.reservation.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

/**
 * Repository for reservation operations with local caching.
 * Implements offline-first pattern for reservation data.
 */
class ReservationRepository(
    private val reservationApi: ReservationApi,
    private val reservationDao: ReservationDao
) {
    // Coroutine scope for background operations
    private val backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    /**
     * Get price quote for a reservation.
     * This is always fetched from API (no caching).
     */
    suspend fun getQuote(request: ReservationQuoteRequestDto): Result<ReservationQuoteResponseDto> {
        return reservationApi.getQuote(request)
    }
    
    /**
     * Get all reservations with optional filters.
     * Offline-first: returns cached data immediately, then syncs.
     */
    suspend fun getReservations(
        renterId: Uuid? = null,
        carId: Uuid? = null,
        status: String? = null,
        start: String? = null,
        end: String? = null,
        forceRefresh: Boolean = false
    ): Result<List<ReservationDto>> {
        // If not forcing refresh and we have cached data, return it
        if (!forceRefresh) {
            val cachedReservations = if (renterId != null) {
                reservationDao.getReservationsByRenter(renterId)
            } else {
                reservationDao.getAllReservations()
            }
            
            if (cachedReservations.isNotEmpty()) {
                // Start background sync
                backgroundScope.launch {
                    syncReservationsInBackground(renterId, carId, status, start, end)
                }
                return Result.success(cachedReservations)
            }
        }
        
        // No cached data or forced refresh - fetch from API
        return reservationApi.getReservations(renterId, carId, status, start, end)
            .onSuccess { reservations ->
                // Cache to database
                reservationDao.insertOrUpdateAll(reservations)
            }.onFailure {
                // On error, return cached data as fallback
                val cachedReservations = reservationDao.getAllReservations()
                if (cachedReservations.isNotEmpty()) {
                    return Result.success(cachedReservations)
                }
            }
    }
    
    /**
     * Background sync for reservations.
     */
    private suspend fun syncReservationsInBackground(
        renterId: Uuid? = null,
        carId: Uuid? = null,
        status: String? = null,
        start: String? = null,
        end: String? = null
    ) {
        try {
            reservationApi.getReservations(renterId, carId, status, start, end)
                .onSuccess { reservations ->
                    reservationDao.insertOrUpdateAll(reservations)
                }
        } catch (e: Exception) {
            // Silently fail background sync
        }
    }
    
    /**
     * Get all reservations as Flow (reactive).
     */
    fun getAllReservationsFlow(): Flow<List<ReservationDto>> {
        return reservationDao.getAllReservationsFlow()
    }
    
    /**
     * Get active reservations.
     * Offline-first: checks database first, then API.
     */
    suspend fun getActiveReservations(forceRefresh: Boolean = false): Result<List<ReservationDto>> {
        // If not forcing refresh and we have cached data, return it
        if (!forceRefresh) {
            val cachedActive = reservationDao.getReservationsByStatus(ReservationStatus.PENDING) +
                               reservationDao.getReservationsByStatus(ReservationStatus.CONFIRMED)
            
            if (cachedActive.isNotEmpty()) {
                // Start background sync
                backgroundScope.launch {
                    syncActiveReservationsInBackground()
                }
                return Result.success(cachedActive)
            }
        }
        
        // No cached data or forced refresh - fetch from API
        return reservationApi.getActiveReservations().onSuccess { reservations ->
            // Cache to database
            reservationDao.insertOrUpdateAll(reservations)
        }.onFailure {
            // On error, return cached data as fallback
            val cachedActive = reservationDao.getReservationsByStatus(ReservationStatus.PENDING) +
                               reservationDao.getReservationsByStatus(ReservationStatus.CONFIRMED)
            if (cachedActive.isNotEmpty()) {
                return Result.success(cachedActive)
            }
        }
    }
    
    /**
     * Background sync for active reservations.
     */
    private suspend fun syncActiveReservationsInBackground() {
        try {
            reservationApi.getActiveReservations().onSuccess { reservations ->
                reservationDao.insertOrUpdateAll(reservations)
            }
        } catch (e: Exception) {
            // Silently fail background sync
        }
    }
    
    /**
     * Get active reservations as Flow (reactive).
     */
    fun getActiveReservationsFlow(): Flow<List<ReservationDto>> {
        return reservationDao.getActiveReservationsFlow()
    }
    
    /**
     * Get reservations for a specific renter as Flow (reactive).
     */
    fun getReservationsByRenterFlow(renterId: Uuid): Flow<List<ReservationDto>> {
        return reservationDao.getReservationsByRenterFlow(renterId)
    }
    
    /**
     * Get a single reservation by ID.
     * Offline-first: checks database first, then API.
     */
    suspend fun getReservation(id: Uuid): Result<ReservationDto> {
        // Check database first
        val cachedReservation = reservationDao.getReservationById(id)
        if (cachedReservation != null) {
            // Start background refresh
            backgroundScope.launch {
                syncReservationInBackground(id)
            }
            return Result.success(cachedReservation)
        }
        
        // Not in cache - fetch from API
        return reservationApi.getReservation(id).onSuccess { reservation ->
            // Cache to database
            reservationDao.insertOrUpdate(reservation)
        }
    }
    
    /**
     * Background sync for single reservation.
     */
    private suspend fun syncReservationInBackground(id: Uuid) {
        try {
            reservationApi.getReservation(id).onSuccess { reservation ->
                reservationDao.insertOrUpdate(reservation)
            }
        } catch (e: Exception) {
            // Silently fail background sync
        }
    }
    
    /**
     * Get reservation by ID as Flow (reactive).
     */
    fun getReservationFlow(id: Uuid): Flow<ReservationDto?> {
        return reservationDao.getReservationByIdFlow(id)
    }
    
    /**
     * Create a new reservation.
     */
    suspend fun createReservation(request: CreateReservationRequestDto): Result<ReservationDto> {
        return reservationApi.createReservation(request).onSuccess { reservation ->
            // Add to database immediately
            reservationDao.insertOrUpdate(reservation)
        }
    }
    
    /**
     * Update an existing reservation.
     */
    suspend fun updateReservation(id: Uuid, request: UpdateReservationRequestDto): Result<ReservationDto> {
        return reservationApi.updateReservation(id, request).onSuccess { reservation ->
            // Update in database
            reservationDao.insertOrUpdate(reservation)
        }
    }
    
    /**
     * Partially update a reservation.
     */
    suspend fun patchReservation(id: Uuid, request: PatchReservationRequestDto): Result<ReservationDto> {
        return reservationApi.patchReservation(id, request).onSuccess { reservation ->
            // Update in database
            reservationDao.insertOrUpdate(reservation)
        }
    }
    
    /**
     * Delete a reservation.
     */
    suspend fun deleteReservation(id: Uuid): Result<Unit> {
        return reservationApi.deleteReservation(id).onSuccess {
            // Remove from database
            reservationDao.deleteById(id)
        }
    }
    
    /**
     * Cancel a reservation.
     */
    suspend fun cancelReservation(id: Uuid): Result<Unit> {
        return reservationApi.cancelReservation(id).onSuccess {
            // Update status in database
            reservationDao.updateStatus(id, ReservationStatus.CANCELLED)
        }
    }
    
    /**
     * Confirm a reservation (owner only).
     */
    suspend fun confirmReservation(id: Uuid): Result<ReservationDto> {
        return reservationApi.confirmReservation(id).onSuccess { reservation ->
            // Update in database
            reservationDao.insertOrUpdate(reservation)
        }
    }
    
    /**
     * Complete a reservation.
     */
    suspend fun completeReservation(id: Uuid): Result<ReservationDto> {
        return reservationApi.completeReservation(id).onSuccess { reservation ->
            // Update in database
            reservationDao.insertOrUpdate(reservation)
        }
    }
    
    /**
     * Get driving sessions for a reservation.
     */
    suspend fun getDrivingSessions(reservationId: Uuid): Result<List<DrivingSessionDto>> {
        // TODO: Implement local caching for driving sessions if needed
        return reservationApi.getDrivingSessions(reservationId)
    }
    
    /**
     * Create a driving session for a reservation.
     */
    suspend fun createDrivingSession(
        reservationId: Uuid,
        request: CreateDrivingSessionRequestDto
    ): Result<DrivingSessionDto> {
        // TODO: Implement local caching for driving sessions if needed
        return reservationApi.createDrivingSession(reservationId, request)
    }
}
