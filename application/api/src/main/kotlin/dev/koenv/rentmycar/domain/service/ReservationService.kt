package dev.koenv.rentmycar.domain.service

import dev.koenv.rentmycar.domain.entity.Reservation
import dev.koenv.rentmycar.domain.enums.ReservationStatus
import dev.koenv.rentmycar.domain.repository.ReservationRepository
import dev.koenv.rentmycar.domain.repository.CarRepository
import dev.koenv.rentmycar.domain.repository.UserRepository
import dev.koenv.rentmycar.shared.http.ApiException
import io.ktor.http.HttpStatusCode
import kotlinx.datetime.LocalDateTime
import java.util.UUID

class ReservationService(
    private val repo: ReservationRepository,
    private val carRepository: CarRepository,
    private val userRepository: UserRepository
) {
    suspend fun getAll(): List<Reservation> = repo.findAll()
    suspend fun getById(id: UUID): Reservation? = repo.findById(id)
    suspend fun getByRenterId(renterId: UUID): List<Reservation> = repo.findByRenterId(renterId)
    suspend fun create(reservation: Reservation): Reservation {
        validateForeignKeys(reservation)
        return repo.create(reservation)
    }

    suspend fun update(id: UUID, reservation: Reservation): Reservation? {
        validateForeignKeys(reservation)
        return repo.update(id, reservation)
    }
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

    private suspend fun validateForeignKeys(reservation: Reservation) {
        if (!carRepository.existsById(reservation.carId)) {
            throw ApiException(
                http = HttpStatusCode.BadRequest,
                code = "CAR_NOT_FOUND",
                message = "Car with id ${reservation.carId} does not exist"
            )
        }
        if (!userRepository.existsById(reservation.renterId)) {
            throw ApiException(
                http = HttpStatusCode.BadRequest,
                code = "RENTER_NOT_FOUND",
                message = "Renter with id ${reservation.renterId} does not exist"
            )
        }
    }
}
