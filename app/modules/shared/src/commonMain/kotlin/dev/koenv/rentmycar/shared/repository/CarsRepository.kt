package dev.koenv.rentmycar.shared.repository

import dev.koenv.rentmycar.shared.api.CarsApi
import dev.koenv.rentmycar.shared.dto.car.CarDto
import dev.koenv.rentmycar.shared.dto.search.SearchResultDto
import dev.koenv.rentmycar.shared.storage.AppDataStorage
import kotlin.uuid.Uuid

/**
 * Repository for car operations.
 * Manages car data fetching and local storage of viewed cars.
 */
class CarsRepository(
    private val carsApi: CarsApi,
    private val appDataStorage: AppDataStorage
) {
    /**
     * Fetches a list of cars with optional filtering.
     * Returns a simple list for basic queries.
     */
    suspend fun getCars(): Result<List<CarDto>> {
        return carsApi.getCars().map { response ->
            when (response) {
                is List<*> -> response.filterIsInstance<CarDto>()
                is SearchResultDto -> response.cars.map { searchDto ->
                    // Convert CarSearchDto to CarDto
                    CarDto(
                        id = searchDto.id,
                        brand = searchDto.brand,
                        model = searchDto.model,
                        category = searchDto.category,
                        ratePerHour = searchDto.ratePerHour,
                        locationLat = searchDto.locationLat,
                        locationLng = searchDto.locationLng,
                        isActive = searchDto.isActive,
                        fuelType = null,  // Not in search result
                        ownerId = Uuid.NIL  // Not in search result
                    )
                }
                else -> emptyList()
            }
        }
    }
    
    /**
     * Searches for cars with pagination and filters.
     */
    suspend fun searchCars(
        page: Int = 1,
        limit: Int = 20,
        latitude: Double? = null,
        longitude: Double? = null,
        maxDistance: Double? = null,
        brand: String? = null
    ): Result<SearchResultDto> {
        return carsApi.getCars(
            page = page,
            limit = limit,
            latitude = latitude,
            longitude = longitude,
            maxDistance = maxDistance,
            brand = brand
        ).map { response ->
            when (response) {
                is SearchResultDto -> response
                else -> SearchResultDto(
                    cars = emptyList(),
                    totalCount = 0,
                    page = page,
                    totalPages = 0,
                    hasNext = false
                )
            }
        }
    }
    
    /**
     * Fetches a single car by ID and marks it as viewed.
     */
    suspend fun getCar(id: Uuid): Result<CarDto> {
        return carsApi.getCar(id).onSuccess {
            appDataStorage.addViewedCar(id)
        }
    }
    
    /**
     * Gets the list of viewed car IDs from local storage.
     */
    fun getViewedCarIds(): List<String> {
        return appDataStorage.getViewedCars()
    }
}
