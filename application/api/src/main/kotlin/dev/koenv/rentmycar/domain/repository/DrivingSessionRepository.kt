package dev.koenv.rentmycar.domain.repository

import dev.koenv.rentmycar.domain.entity.DrivingSession
import java.util.UUID

interface DrivingSessionRepository :
    Repository<DrivingSession, UUID> {
    suspend fun findByReservationId(reservationId: UUID): List<DrivingSession>
}