package dev.koenv.rentmycar.shared.db.dao

import dev.koenv.rentmycar.shared.db.Car
import dev.koenv.rentmycar.shared.db.DatabaseManager
import dev.koenv.rentmycar.shared.domain.enums.CarCategory
import dev.koenv.rentmycar.shared.domain.enums.FuelType
import dev.koenv.rentmycar.shared.dto.car.CarDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlin.uuid.Uuid

/**
 * Data Access Object for Car operations.
 * Handles conversion between CarDto and database Car entity.
 */
class CarDao(private val databaseManager: DatabaseManager) {
    
    /**
     * Get all cars as a Flow.
     */
    fun getAllCarsFlow(): Flow<List<CarDto>> {
        return databaseManager.getAllCarsFlow().map { cars -> cars.map { it.toDto() } }
    }
    
    /**
     * Get all active cars as a Flow.
     */
    fun getActiveCarsFlow(): Flow<List<CarDto>> {
        return databaseManager.getActiveCarsFlow().map { cars -> cars.map { it.toDto() } }
    }
    
    /**
     * Get car by ID as a Flow.
     */
    fun getCarByIdFlow(carId: Uuid): Flow<CarDto?> {
        return databaseManager.getCarByIdFlow(carId.toString()).map { car -> car?.toDto() }
    }
    
    /**
     * Get all cars synchronously.
     */
    fun getAllCars(): List<CarDto> {
        return databaseManager.carQueries.selectAll().executeAsList().map { it.toDto() }
    }
    
    /**
     * Get car by ID synchronously.
     */
    fun getCarById(carId: Uuid): CarDto? {
        return databaseManager.carQueries.selectById(carId.toString()).executeAsOneOrNull()?.toDto()
    }
    
    /**
     * Get cars by owner ID.
     */
    fun getCarsByOwner(ownerId: Uuid): List<CarDto> {
        return databaseManager.carQueries.selectByOwner(ownerId.toString()).executeAsList().map { it.toDto() }
    }
    
    /**
     * Insert or update a car.
     */
    fun insertOrUpdate(carDto: CarDto) {
        val now = Clock.System.now().toEpochMilliseconds()
        databaseManager.carQueries.insertOrReplace(
            id = carDto.id.toString(),
            brand = carDto.brand,
            model = carDto.model,
            category = carDto.category.name,
            fuelType = carDto.fuelType?.name,
            ratePerHour = carDto.ratePerHour.toString(),
            locationLat = carDto.locationLat,
            locationLng = carDto.locationLng,
            isActive = if (carDto.isActive) 1 else 0,
            ownerId = carDto.ownerId.toString(),
            createdAt = now,
            updatedAt = now
        )
    }
    
    /**
     * Insert or update multiple cars.
     */
    fun insertOrUpdateAll(cars: List<CarDto>) {
        databaseManager.transaction {
            cars.forEach { insertOrUpdate(it) }
        }
    }
    
    /**
     * Delete car by ID.
     */
    fun deleteById(carId: Uuid) {
        databaseManager.carQueries.deleteById(carId.toString())
    }
    
    /**
     * Delete all cars.
     */
    fun deleteAll() {
        databaseManager.carQueries.deleteAll()
    }
    
    /**
     * Count all cars.
     */
    fun countAll(): Long {
        return databaseManager.carQueries.countAll().executeAsOne()
    }
    
    /**
     * Count active cars.
     */
    fun countActive(): Long {
        return databaseManager.carQueries.countActive().executeAsOne()
    }
    
    private fun Car.toDto(): CarDto {
        return CarDto(
            id = Uuid.parse(this.id),
            brand = this.brand,
            model = this.model,
            category = CarCategory.valueOf(this.category),
            fuelType = this.fuelType?.let { FuelType.valueOf(it) },
            ratePerHour = com.ionspin.kotlin.bignum.decimal.BigDecimal.parseString(this.ratePerHour),
            locationLat = this.locationLat ?: 0.0,
            locationLng = this.locationLng ?: 0.0,
            isActive = this.isActive == 1L,
            ownerId = Uuid.parse(this.ownerId)
        )
    }
}
