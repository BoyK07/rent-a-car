package dev.koenv.rentmycar.domain.service

import dev.koenv.rentmycar.domain.entity.CarAvailability
import dev.koenv.rentmycar.domain.repository.CarAvailabilityRepository
import kotlinx.datetime.LocalDateTime
import java.util.UUID

class CarAvailabilityService(private val repo: CarAvailabilityRepository) {
    suspend fun getAll(): List<CarAvailability> = repo.findAll()
    suspend fun getById(id: UUID): CarAvailability? = repo.findById(id)
    suspend fun getByCarId(carId: UUID): List<CarAvailability> = repo.findByCarId(carId)
    suspend fun create(availability: CarAvailability): CarAvailability = repo.create(availability)
    suspend fun update(id: UUID, availability: CarAvailability): CarAvailability? = repo.update(id, availability)
    suspend fun delete(id: UUID): Boolean = repo.delete(id)

    suspend fun findAvailableCarsBetween(start: LocalDateTime, end: LocalDateTime): List<CarAvailability> {
        return repo.findAll().filter {
            it.startTime <= start && it.endTime >= end
        }
    }
}
