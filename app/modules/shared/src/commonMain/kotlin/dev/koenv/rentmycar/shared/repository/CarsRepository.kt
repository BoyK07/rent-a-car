package dev.koenv.rentmycar.shared.repository

import dev.koenv.rentmycar.shared.api.CarsApi
import dev.koenv.rentmycar.shared.db.dao.CarDao
import dev.koenv.rentmycar.shared.dto.car.CarDto
import dev.koenv.rentmycar.shared.dto.car.CreateCarRequestDto
import dev.koenv.rentmycar.shared.dto.car.PatchCarRequestDto
import dev.koenv.rentmycar.shared.dto.car.UpdateCarRequestDto
import dev.koenv.rentmycar.shared.dto.search.SearchResultDto
import dev.koenv.rentmycar.shared.storage.AppDataStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

/**
 * Repository for car listing operations with offline-first architecture.
 * 
 * Features:
 * - Offline-first pattern: Local DB → UI → API sync in background
 * - Real-time Flow updates from database
 * - Car CRUD operations (create, read, update, delete)
 * - Search with filters (brand, category, location, etc.)
 * - Photo management (upload via API)
 * - Availability window management
 * - View tracking in local storage
 * - Background sync with API
 * 
 * Data flow:
 * 1. Read from local DB immediately (fast, works offline)
 * 2. Display cached data to user
 * 3. Sync with API in background
 * 4. Update local DB with fresh data
 * 5. UI automatically updates via Flow
 */
class CarsRepository(
    private val carsApi: CarsApi,
    private val carDao: CarDao,
    private val appDataStorage: AppDataStorage
) {
    // Coroutine scope for background operations
    private val backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    /**
     * Returns real-time Flow of active cars from local database.
     * UI automatically updates when database changes.
     * 
     * @return Flow emitting updated car lists on any database change
     */
    fun getCarsFlow(): Flow<List<CarDto>> {
        // Return database flow that updates in real-time
        return carDao.getActiveCarsFlow()
    }

    
    /**
     * Fetches cars with offline-first strategy.
     * Returns cached data immediately if available, then syncs with API.
     * 
     * @param forceRefresh Skip cache and fetch directly from API
     * @return Result containing list of cars or error
     */
    suspend fun getCars(forceRefresh: Boolean = false): Result<List<CarDto>> {
        if (!forceRefresh) {
            val cachedCars = carDao.getAllCars()
            if (cachedCars.isNotEmpty()) {
                // Start background sync
                backgroundScope.launch {
                    syncCarsInBackground()
                }
                return Result.success(cachedCars)
            }
        }
        
        // No cached data or forced refresh - fetch from API
        return carsApi.getCars().map { response ->
            when (response) {
                is List<*> -> {
                    val cars = response.filterIsInstance<CarDto>()
                    // Cache to database
                    carDao.insertOrUpdateAll(cars)
                    cars
                }
                is SearchResultDto -> {
                    val cars = response.cars.map { searchDto ->
                        // Convert CarSearchDto to CarDto
                        CarDto(
                            id = searchDto.id,
                            brand = searchDto.brand,
                            model = searchDto.model,
                            category = searchDto.category,
                            ratePerHour = searchDto.ratePerHour,
                            addressLine1 = null,
                            addressLine2 = null,
                            postalCode = null,
                            city = null,
                            country = null,
                            formattedAddress = null,
                            locationLat = searchDto.locationLat,
                            locationLng = searchDto.locationLng,
                            isActive = searchDto.isActive,
                            fuelType = null,  // Not in search result
                            ownerId = Uuid.NIL  // Not in search result
                        )
                    }
                    // Cache to database
                    carDao.insertOrUpdateAll(cars)
                    cars
                }
                else -> emptyList()
            }
        }.onFailure {
            // On error, return cached data as fallback
            val cachedCars = carDao.getAllCars()
            if (cachedCars.isNotEmpty()) {
                return Result.success(cachedCars)
            }
        }
    }
    
    /**
     * Background sync from API to database.
     */
    private suspend fun syncCarsInBackground() {
        try {
            carsApi.getCars().onSuccess { response ->
                when (response) {
                    is List<*> -> {
                        val cars = response.filterIsInstance<CarDto>()
                        carDao.insertOrUpdateAll(cars)
                    }
                    is SearchResultDto -> {
                        val cars = response.cars.map { searchDto ->
                            CarDto(
                                id = searchDto.id,
                                brand = searchDto.brand,
                                model = searchDto.model,
                                category = searchDto.category,
                                ratePerHour = searchDto.ratePerHour,
                                addressLine1 = null,
                                addressLine2 = null,
                                postalCode = null,
                                city = null,
                                country = null,
                                formattedAddress = null,
                                locationLat = searchDto.locationLat,
                                locationLng = searchDto.locationLng,
                                isActive = searchDto.isActive,
                                fuelType = null,
                                ownerId = Uuid.NIL
                            )
                        }
                        carDao.insertOrUpdateAll(cars)
                    }
                }
            }
        } catch (e: Exception) {
            // Silently fail background sync - offline mode
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
     * Fetches a single car by ID.
     * Offline-first: checks database first, then API.
     */
    suspend fun getCar(id: Uuid): Result<CarDto> {
        // Check database first
        val cachedCar = carDao.getCarById(id)
        if (cachedCar != null) {
            // Track as viewed even from cache
            appDataStorage.addViewedCar(id)
            // Start background refresh
            backgroundScope.launch {
                syncCarInBackground(id)
            }
            return Result.success(cachedCar)
        }
        
        // Not in cache - fetch from API
        return carsApi.getCar(id).onSuccess { car ->
            // Cache to database
            carDao.insertOrUpdate(car)
            // Mark as viewed
            appDataStorage.addViewedCar(id)
        }
    }
    
    /**
     * Background sync for single car.
     */
    private suspend fun syncCarInBackground(id: Uuid) {
        try {
            carsApi.getCar(id).onSuccess { car ->
                carDao.insertOrUpdate(car)
            }
        } catch (e: Exception) {
            // Silently fail background sync
        }
    }
    
    /**
     * Creates a new car (requires authentication).
     */
    suspend fun createCar(request: CreateCarRequestDto): Result<CarDto> {
        return carsApi.createCar(request).onSuccess { car ->
            // Add to database immediately
            carDao.insertOrUpdate(car)
        }
    }
    
    /**
     * Updates an existing car (requires authentication).
     */
    suspend fun updateCar(id: Uuid, request: UpdateCarRequestDto): Result<CarDto> {
        return carsApi.updateCar(id, request).onSuccess { car ->
            // Update in database
            carDao.insertOrUpdate(car)
        }
    }
    
    /**
     * Partially updates a car (requires authentication).
     */
    suspend fun patchCar(id: Uuid, request: PatchCarRequestDto): Result<CarDto> {
        return carsApi.patchCar(id, request).onSuccess { car ->
            // Update in database
            carDao.insertOrUpdate(car)
        }
    }
    
    /**
     * Deletes a car (requires authentication).
     */
    suspend fun deleteCar(id: Uuid): Result<Unit> {
        return carsApi.deleteCar(id).onSuccess {
            // Remove from database
            carDao.deleteById(id)
        }
    }
    
    /**
     * Gets the list of viewed car IDs from local storage.
     */
    fun getViewedCarIds(): List<String> {
        return appDataStorage.getViewedCars()
    }
    
    /**
     * Get car by ID as Flow (reactive).
     */
    fun getCarFlow(id: Uuid): Flow<CarDto?> {
        return carDao.getCarByIdFlow(id)
    }
}
