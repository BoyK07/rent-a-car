package dev.koenv.rentmycar.domain.service

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
        brand: String? = null,
        page: Int = 1,
        limit: Int = 20
    ): SearchResultDto {

        // Validate fuel type against categories
        validateFuelTypeForCategory(category, fuelType)

        val cars = carRepository.searchCars(
            latitude,
            longitude,
            maxDistance,
            minPrice,
            maxPrice,
            category,
            fuelType,
            brand
        )

        // exact Haversine filter if geo (lat/long) provided
        val withDistance = cars.map { car ->
            val d = if (latitude != null && longitude != null)
                calculateDistance(latitude, longitude, car.locationLat, car.locationLng)
            else null
            car to d
        }

        val filtered = if (latitude != null && longitude != null && maxDistance != null) {
            withDistance.filter { it.second != null && it.second!! <= maxDistance }
        } else withDistance

        val totalCount = filtered.size
        val totalPages = ceil(totalCount.toDouble() / limit).toInt().coerceAtLeast(1)
        val hasNext = page < totalPages

        val offset = (page - 1) * limit
        val pageSlice = filtered.drop(offset).take(limit)

        val carDtos = pageSlice.map { (car, d) ->
            CarSearchDto(
                id = car.id!!,
                brand = car.brand,
                model = car.model,
                category = car.category,
                ratePerHour = car.ratePerHour,
                distance = d,
                locationLat = car.locationLat,
                locationLng = car.locationLng,
                thumbnailUrl = null,
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
     * Search nearby cars
     */
    suspend fun searchNearbyCars(request: NearbySearchRequestDto): SearchResultDto {
        val cars = carRepository.findNearbyCars(
            request.latitude,
            request.longitude,
            request.radius,
            request.limit * request.page
        )

        val withDistance = cars.map { car ->
            val d = calculateDistance(request.latitude, request.longitude, car.locationLat, car.locationLng)
            car to d
        }

        val within = withDistance.filter { it.second <= request.radius }
            .sortedBy { it.second }

        val totalCount = within.size
        val totalPages = ceil(totalCount.toDouble() / request.limit).toInt().coerceAtLeast(1)
        val page = request.page.coerceAtLeast(1)
        val offset = (page - 1) * request.limit
        val slice = within.drop(offset).take(request.limit)

        val carDtos = slice.map { (car, d) ->
            CarSearchDto(
                id = car.id!!,
                brand = car.brand,
                model = car.model,
                category = car.category,
                ratePerHour = car.ratePerHour,
                distance = d,
                locationLat = car.locationLat,
                locationLng = car.locationLng,
                thumbnailUrl = null,
                isActive = car.isActive
            )
        }

        return SearchResultDto(
            cars = carDtos,
            totalCount = totalCount,
            page = page,
            totalPages = totalPages,
            hasNext = page < totalPages
        )
    }

    /**
     * Calculate distance between two sets of coordinates using the Haversine formula
     */
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371.0 // earth radius in kilometers

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadius * c
    }

    /**
     * Validate if fuelType is compatible with the category
     */
    private fun validateFuelTypeForCategory(category: CarCategory?, fuelType: FuelType?) {
        if (category != null && fuelType != null) {
            val validFuelTypes = when (category) {
                CarCategory.ICE -> listOf(FuelType.PETROL, FuelType.DIESEL, FuelType.LPG)
                CarCategory.BEV -> listOf(FuelType.ELECTRIC)
                CarCategory.FCEV -> listOf(FuelType.HYBRIDE) // Gebruik HYBRIDE voor FCEV
            }

            if (fuelType !in validFuelTypes) {
                throw IllegalArgumentException("FuelType $fuelType is not compatible with Category $category")
            }
        }
    }
}
