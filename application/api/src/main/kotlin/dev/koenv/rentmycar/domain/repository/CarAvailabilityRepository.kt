package dev.koenv.rentmycar.domain.repository

import dev.koenv.rentmycar.domain.entity.CarAvailability
import java.util.UUID

interface CarAvailabilityRepository :
    ReadRepository<CarAvailability, UUID>,
    WriteRepository<CarAvailability, UUID> {
    suspend fun findByCarId(carId: UUID): List<CarAvailability>
}