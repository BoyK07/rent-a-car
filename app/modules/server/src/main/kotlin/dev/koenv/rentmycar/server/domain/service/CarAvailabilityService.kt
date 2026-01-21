package dev.koenv.rentmycar.server.domain.service

import dev.koenv.rentmycar.server.storage.repository.CarAvailabilityRepositoryImpl
import dev.koenv.rentmycar.shared.domain.entity.CarAvailability
import kotlinx.datetime.LocalDateTime
import kotlin.uuid.Uuid

/**
 * Service layer for managing car availability windows.
 * 
 * Handles operations related to when cars are available for rental.
 * Each availability window represents a time range when a car can be rented.
 * 
 * Used for:
 * - Defining when cars are available
 * - Finding cars available during specific time periods
 * - Filtering availability windows by car and time range
 * 
 * @property repo The car availability repository for data persistence
 */
class CarAvailabilityService(private val repo: CarAvailabilityRepositoryImpl) {
    
    /**
     * Retrieves all availability windows in the system.
     * 
     * @return List of all car availability records
     */
    suspend fun getAll(): List<CarAvailability> = repo.findAll()
    
    /**
     * Finds a specific availability window by its unique identifier.
     * 
     * @param id The UUID of the availability record to retrieve
     * @return The availability record if found, null otherwise
     */
    suspend fun getById(id: Uuid): CarAvailability? = repo.findById(id)
    
    /**
     * Retrieves all availability windows for a specific car.
     * 
     * @param carId The UUID of the car whose availability to retrieve
     * @return List of availability windows for the specified car
     */
    suspend fun getByCarId(carId: Uuid): List<CarAvailability> = repo.findByCarId(carId)
    
    /**
     * Creates a new availability window for a car.
     * 
     * @param availability The availability record to create (must include carId, startTime, endTime)
     * @return The created availability record with generated ID
     */
    suspend fun create(availability: CarAvailability): CarAvailability = repo.create(availability)
    
    /**
     * Updates an existing availability window.
     * 
     * @param id The UUID of the availability record to update
     * @param availability The updated availability data
     * @return The updated record if found, null if record doesn't exist
     */
    suspend fun update(id: Uuid, availability: CarAvailability): CarAvailability? = 
        repo.update(id, availability)
    
    /**
     * Deletes an availability window.
     * 
     * @param id The UUID of the availability record to delete
     * @return true if record was deleted, false if record didn't exist
     */
    suspend fun delete(id: Uuid): Boolean = repo.delete(id)

    /**
     * Finds cars that are available for the entire specified time range.
     * 
     * A car is considered available if there exists an availability window
     * that completely covers the requested time period.
     * 
     * Logic: availability.startTime <= requestedStart AND availability.endTime >= requestedEnd
     * 
     * @param start The start of the desired rental period
     * @param end The end of the desired rental period
     * @return List of availability records that cover the entire requested period
     */
    suspend fun findAvailableCarsBetween(start: LocalDateTime, end: LocalDateTime): List<CarAvailability> {
        return repo.findAll().filter {
            it.startTime <= start && it.endTime >= end
        }
    }

    /**
     * Filters availability records by car and/or time range.
     * 
     * All filter parameters are optional. When null, that filter is ignored.
     * Time filtering checks if the availability window overlaps with the specified range.
     * 
     * @param carId Optional car UUID to filter by
     * @param startTime Optional start time - finds windows that start on or before this time
     * @param endTime Optional end time - finds windows that end on or after this time
     * @return List of availability records matching the filter criteria
     */
    suspend fun listFiltered(
        carId: Uuid? = null,
        startTime: LocalDateTime? = null,
        endTime: LocalDateTime? = null
    ): List<CarAvailability> {
        // Get base set of records (by car or all)
        val all = when {
            carId != null -> repo.findByCarId(carId)
            else -> repo.findAll()
        }

        // Apply time range filters
        return all.filter { availability ->
            (startTime == null || availability.startTime <= startTime) &&
            (endTime == null || availability.endTime >= endTime)
        }
    }
}
