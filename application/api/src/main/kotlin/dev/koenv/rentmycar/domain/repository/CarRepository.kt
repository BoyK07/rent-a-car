package dev.koenv.rentmycar.domain.repository

import dev.koenv.rentmycar.domain.entity.Car
import dev.koenv.rentmycar.domain.enums.CarCategory
import dev.koenv.rentmycar.domain.enums.FuelType
import java.math.BigDecimal
import java.util.*

interface CarRepository : Repository<Car, UUID> {

    /**
     * Search cars based on multiple possible criteria
     */
    suspend fun searchCars(
        latitude: Double? = null,
        longitude: Double? = null,
        maxDistance: Double? = null,
        minPrice: BigDecimal? = null,
        maxPrice: BigDecimal? = null,
        category: CarCategory? = null,
        fuelType: FuelType? = null,
        brand: String? = null
    ): List<Car>
    
    /**
     * Count the amount of found cars based on multiple possible criteria
     */
    suspend fun countSearchResults(
        latitude: Double? = null,
        longitude: Double? = null,
        maxDistance: Double? = null,
        minPrice: BigDecimal? = null,
        maxPrice: BigDecimal? = null,
        category: CarCategory? = null,
        fuelType: FuelType? = null,
        brand: String? = null
    ): Int
    
    /**
     * Search cars in a radius of the given coordinates (with a limit)
     */
    suspend fun findNearbyCars(
        latitude: Double,
        longitude: Double,
        radius: Double,
        limit: Int
    ): List<Car>
}