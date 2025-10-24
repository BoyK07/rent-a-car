package dev.koenv.rentmycar.domain.service

import dev.koenv.rentmycar.domain.entity.Car
import dev.koenv.rentmycar.domain.enums.CarCategory
import dev.koenv.rentmycar.domain.enums.FuelType
import dev.koenv.rentmycar.domain.repository.CarRepository
import kotlinx.datetime.LocalDateTime
import java.math.BigDecimal
import java.util.UUID

class CarService(private val repo: CarRepository) {
    suspend fun getAll(): List<Car> = repo.findAll()
    suspend fun getById(id: UUID): Car? = repo.findById(id)
    suspend fun create(car: Car): Car = repo.create(car)
    suspend fun update(id: UUID, car: Car): Car? = repo.update(id, car)
    suspend fun delete(id: UUID): Boolean = repo.delete(id)
    suspend fun count(): Long = repo.count()

    suspend fun listFiltered(
        ownerId: UUID? = null,
        category: CarCategory? = null,
        fuelType: FuelType? = null,
        isActive: Boolean? = null,
        maxRate: BigDecimal? = null
    ): List<Car> {
        return repo.findAll().asSequence()
            .filter { ownerId == null || it.ownerId == ownerId }
            .filter { category == null || it.category == category }
            .filter { fuelType == null || it.fuelType == fuelType }
            .filter { isActive == null || it.isActive == isActive }
            .filter { maxRate == null || it.ratePerHour <= maxRate }
            .toList()
    }

    suspend fun findAvailableCarsInRange(
        start: LocalDateTime,
        end: LocalDateTime,
        maxRate: BigDecimal? = null
    ): List<Car> {
        TODO("Filter by availability and rate using repositories")
    }

    suspend fun findCarsNearLocation(
        latitude: Double,
        longitude: Double,
        radiusKm: Double
    ): List<Car> {
        TODO("Return cars within radiusKm of given coordinates")
    }

    fun calculateTcoPerYear(car: Car, annualKm: Int = 15000): BigDecimal {
        TODO("Compute annual TCO based on car category and usage")
    }

    fun calculateCostPerKm(car: Car): BigDecimal {
        TODO("Compute per-kilometer cost depending on category and fuel type")
    }
}
