package dev.koenv.rentmycar.domain.service

import dev.koenv.rentmycar.domain.entity.DrivingSession
import dev.koenv.rentmycar.domain.repository.DrivingSessionRepository
import java.util.UUID

class DrivingSessionService(private val repo: DrivingSessionRepository) {
    suspend fun getAll(): List<DrivingSession> = repo.findAll()
    suspend fun getById(id: UUID): DrivingSession? = repo.findById(id)
    suspend fun getByReservationId(reservationId: UUID): List<DrivingSession> = repo.findByReservationId(reservationId)
    suspend fun create(session: DrivingSession): DrivingSession = repo.create(session)
    suspend fun update(id: UUID, session: DrivingSession): DrivingSession? = repo.update(id, session)
    suspend fun delete(id: UUID): Boolean = repo.delete(id)
}
