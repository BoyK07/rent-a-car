package dev.koenv.rentmycar.domain.repository

import dev.koenv.rentmycar.domain.entity.Car
import dev.koenv.rentmycar.domain.enums.CarCategory
import dev.koenv.rentmycar.domain.enums.FuelType
import java.math.BigDecimal
import java.util.*

interface CarRepository : Repository<Car, UUID> {
    
    /**
     * Zoek auto's op basis van verschillende criteria
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
     * Tel totaal aantal auto's dat voldoet aan zoekcriteria
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
     * Zoek auto's in de buurt op basis van GPS coördinaten
     */
    suspend fun findNearbyCars(
        latitude: Double,
        longitude: Double,
        radius: Double,
        limit: Int
    ): List<Car>
}