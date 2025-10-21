package dev.koenv.rentmycar.domain.repository

import dev.koenv.rentmycar.domain.entity.Reservation
import java.util.UUID

interface ReservationRepository : Repository<Reservation, UUID> {
    suspend fun findByRenterId(renterId: UUID): List<Reservation>
}