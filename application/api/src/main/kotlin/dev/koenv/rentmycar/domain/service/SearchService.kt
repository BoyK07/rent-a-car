package dev.koenv.rentmycar.domain.service

import dev.koenv.rentmycar.domain.entity.Car
import dev.koenv.rentmycar.domain.enums.CarCategory
import dev.koenv.rentmycar.domain.enums.FuelType
import dev.koenv.rentmycar.domain.repository.CarRepository
import dev.koenv.rentmycar.dto.search.CarSearchDto
import dev.koenv.rentmycar.dto.search.NearbySearchRequestDto
import dev.koenv.rentmycar.dto.search.SearchResultDto
import kotlin.math.*
import java.math.BigDecimal

class SearchService(
    private val carRepository: CarRepository
) {
    
    /**
     * Zoek auto's met verschillende filter criteria
     */
    suspend fun searchCars(
        latitude: Double? = null,
        longitude: Double? = null,
        maxDistance: Double? = null,
        minPrice: BigDecimal? = null,
        maxPrice: BigDecimal? = null,
        category: CarCategory? = null,
        fuelType: FuelType? = null,
        brand: String? = null,
        page: Int = 1,
        limit: Int = 20
    ): SearchResultDto {
        
        // Valideer fuelType tegen category
        validateFuelTypeForCategory(category, fuelType)
        
        val cars = carRepository.searchCars(
            latitude, longitude, maxDistance,
            minPrice, maxPrice, category, fuelType,
            brand
        )
        
        val totalCount = carRepository.countSearchResults(
            latitude, longitude, maxDistance,
            minPrice, maxPrice, category, fuelType,
            brand
        )
        
        val totalPages = ceil(totalCount.toDouble() / limit).toInt()
        val hasNext = page < totalPages
        
        // Implementeer paginering in de service
        val offset = (page - 1) * limit
        val paginatedCars = cars.drop(offset).take(limit)
        
        // Bereken afstand voor elke auto als locatie zoeken wordt gebruikt
        val carDtos = paginatedCars.map { car ->
            val distance = if (latitude != null && longitude != null) {
                calculateDistance(latitude, longitude, car.locationLat, car.locationLng)
            } else null
            
            CarSearchDto(
                id = car.id!!,
                brand = car.brand,
                model = car.model,
                category = car.category,
                ratePerHour = car.ratePerHour,
                distance = distance,
                locationLat = car.locationLat,
                locationLng = car.locationLng,
                thumbnailUrl = null, // TODO: Implementeer foto functionaliteit
                isActive = car.isActive
            )
        }
        
        return SearchResultDto(
            cars = carDtos,
            totalCount = totalCount,
            page = page,
            totalPages = totalPages,
            hasNext = hasNext
        )
    }
    
    /**
     * Zoek auto's in de buurt
     */
    suspend fun searchNearbyCars(request: NearbySearchRequestDto): List<CarSearchDto> {
        val cars = carRepository.findNearbyCars(
            request.latitude,
            request.longitude,
            request.radius,
            request.limit
        )
        
        return cars.map { car ->
            val distance = calculateDistance(
                request.latitude, request.longitude,
                car.locationLat, car.locationLng
            )
            
            CarSearchDto(
                id = car.id!!,
                brand = car.brand,
                model = car.model,
                category = car.category,
                ratePerHour = car.ratePerHour,
                distance = distance,
                locationLat = car.locationLat,
                locationLng = car.locationLng,
                thumbnailUrl = null,
                isActive = car.isActive
            )
        }.sortedBy { it.distance } // Sorteer op afstand
    }
    
    /**
     * Bereken afstand tussen twee GPS punten met Haversine formule
     */
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371.0 // Aardradius in kilometers
        
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        
        return earthRadius * c
    }
    
    /**
     * Valideer of fuelType compatibel is met category
     */
    private fun validateFuelTypeForCategory(category: CarCategory?, fuelType: FuelType?) {
        if (category != null && fuelType != null) {
            val validFuelTypes = when (category) {
                CarCategory.ICE -> listOf(FuelType.PETROL, FuelType.DIESEL, FuelType.LPG)
                CarCategory.BEV -> listOf(FuelType.ELECTRIC)
                CarCategory.FCEV -> listOf(FuelType.HYBRIDE) // Gebruik HYBRIDE voor FCEV
            }
            
            if (fuelType !in validFuelTypes) {
                throw IllegalArgumentException("FuelType $fuelType is niet compatibel met Category $category")
            }
        }
    }
}
