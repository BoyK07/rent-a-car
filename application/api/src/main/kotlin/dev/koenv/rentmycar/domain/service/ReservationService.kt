package dev.koenv.rentmycar.domain.service

import dev.koenv.rentmycar.domain.entity.Reservation
import dev.koenv.rentmycar.domain.enums.ReservationStatus
import dev.koenv.rentmycar.domain.repository.ReservationRepository
import kotlinx.datetime.LocalDateTime
import java.util.UUID

class ReservationService(private val repo: ReservationRepository) {
    suspend fun getAll(): List<Reservation> = repo.findAll()
    suspend fun getById(id: UUID): Reservation? = repo.findById(id)
    suspend fun getByRenterId(renterId: UUID): List<Reservation> = repo.findByRenterId(renterId)
    suspend fun create(reservation: Reservation): Reservation = repo.create(reservation)
    suspend fun update(id: UUID, reservation: Reservation): Reservation? = repo.update(id, reservation)
    suspend fun delete(id: UUID): Boolean = repo.delete(id)

    suspend fun cancel(id: UUID): Boolean {
        val reservation = repo.findById(id) ?: return false
        if (reservation.status == ReservationStatus.COMPLETED || reservation.status == ReservationStatus.CANCELLED)
            return false
        val updated = reservation.copy(status = ReservationStatus.CANCELLED)
        repo.update(id, updated)
        return true
    }

    suspend fun findActiveReservations(now: LocalDateTime): List<Reservation> =
        repo.findAll().filter { it.startTime <= now && it.endTime >= now }
}
