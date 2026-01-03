package dev.koenv.rentmycar.server.domain.service

import dev.koenv.rentmycar.server.storage.repository.CarAvailabilityRepositoryImpl
import dev.koenv.rentmycar.shared.domain.entity.CarAvailability
import kotlinx.datetime.LocalDateTime
import kotlin.uuid.Uuid

class CarAvailabilityService(private val repo: CarAvailabilityRepositoryImpl) {
    suspend fun getAll(): List<CarAvailability> = repo.findAll()
    suspend fun getById(id: Uuid): CarAvailability? = repo.findById(id)
    suspend fun getByCarId(carId: Uuid): List<CarAvailability> = repo.findByCarId(carId)
    suspend fun create(availability: CarAvailability): CarAvailability = repo.create(availability)
    suspend fun update(id: Uuid, availability: CarAvailability): CarAvailability? = repo.update(id, availability)
    suspend fun delete(id: Uuid): Boolean = repo.delete(id)

    suspend fun findAvailableCarsBetween(start: LocalDateTime, end: LocalDateTime): List<CarAvailability> {
        return repo.findAll().filter {
            it.startTime <= start && it.endTime >= end
        }
    }

    suspend fun listFiltered(
        carId: Uuid? = null,
        startTime: LocalDateTime? = null,
        endTime: LocalDateTime? = null
    ): List<CarAvailability> {
        val all = when {
            carId != null -> repo.findByCarId(carId)
            else -> repo.findAll()
        }

        return all.filter { availability ->
            (startTime == null || availability.startTime <= startTime) &&
                    (endTime == null || availability.endTime >= endTime)
        }
    }
}
