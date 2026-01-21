package dev.koenv.rentmycar.server.domain.service

import dev.koenv.rentmycar.server.storage.repository.DrivingSessionRepositoryImpl
import dev.koenv.rentmycar.shared.domain.entity.DrivingSession
import dev.koenv.rentmycar.shared.domain.enums.ReservationStatus
import dev.koenv.rentmycar.shared.http.ApiException
import io.ktor.http.*
import kotlin.uuid.Uuid

class DrivingSessionService(
    private val repo: DrivingSessionRepositoryImpl,
    private val reservationService: ReservationService
) {
    suspend fun getAll(): List<DrivingSession> = repo.findAll()
    suspend fun getById(id: Uuid): DrivingSession? = repo.findById(id)
    suspend fun getByReservationId(reservationId: Uuid): List<DrivingSession> = repo.findByReservationId(reservationId)

    /**
     * Creates a driving session with validation and automatic point calculation.
     */
    suspend fun create(session: DrivingSession): DrivingSession {
        // Validate reservation exists and is active
        val reservation = reservationService.getById(session.reservationId)
            ?: throw ApiException(HttpStatusCode.NotFound, message = "Reservation not found")

        if (reservation.status != ReservationStatus.CONFIRMED) {
            throw ApiException(
                HttpStatusCode.BadRequest,
                code = "INVALID_RESERVATION_STATUS",
                message = "Can only record driving sessions for confirmed reservations"
            )
        }

        // Validate session is within reservation timeframe
        if (session.startTime < reservation.startTime || session.endTime > reservation.endTime) {
            throw ApiException(
                HttpStatusCode.BadRequest,
                code = "INVALID_TIME_RANGE",
                message = "Driving session must be within reservation timeframe"
            )
        }

        // Validate session times
        if (session.endTime <= session.startTime) {
            throw ApiException(
                HttpStatusCode.BadRequest,
                code = "INVALID_TIME_RANGE",
                message = "End time must be after start time"
            )
        }

        // Validate recorded by is the renter
        if (session.recordedBy != reservation.renterId) {
            throw ApiException(
                HttpStatusCode.Forbidden,
                code = "UNAUTHORIZED",
                message = "Only the renter can record driving sessions"
            )
        }

        // Calculate and award points
        val points = calculateDrivingPoints(session.distanceKm, session.harshAccelerations, session.harshBrakes)

        // Create session
        val created = repo.create(session)

        // Update reservation points
        reservationService.addPoints(session.reservationId, points)

        return created
    }

    suspend fun update(id: Uuid, session: DrivingSession): DrivingSession? = repo.update(id, session)
    suspend fun delete(id: Uuid): Boolean = repo.delete(id)

    /**
     * Calculates bonus points based on driving behavior.
     * Rewards safe driving with points, penalizes harsh driving.
     */
    fun calculateDrivingPoints(
        distanceKm: Double,
        harshAccelerations: Int,
        harshBrakes: Int
    ): Int {
        // Base points: 10 per 10km driven
        var points = (distanceKm / 10.0).toInt() * 10

        // Penalty for harsh driving
        points -= harshAccelerations * 5
        points -= harshBrakes * 5

        // Ensure non-negative points
        return points.coerceAtLeast(0)
    }
}
