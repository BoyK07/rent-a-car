package dev.koenv.rentmycar.shared.storage

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.uuid.Uuid

/**
 * Storage for application data that needs to persist locally.
 * Used for storing non-auth data like viewed cars, preferences, etc.
 */
class AppDataStorage(
    private val settings: Settings = SettingsFactory.createSettings()
) {
    companion object {
        private const val KEY_VIEWED_CARS = "viewed_cars"
        private const val KEY_USER_PREFERENCES = "user_preferences"
        private const val MAX_VIEWED_CARS = 100 // Limit stored car history
    }
    
    // Shared JSON configuration for consistency
    private val json = Json {
        prettyPrint = false
        ignoreUnknownKeys = true
    }
    
    // In-memory cache for viewed cars to reduce serialization overhead
    private var viewedCarsCache: MutableSet<String>? = null
    
    /**
     * Adds a car ID to the list of viewed cars.
     * Uses in-memory cache to minimize serialization operations.
     */
    fun addViewedCar(carId: Uuid) {
        val viewedCars = getViewedCarsInternal()
        if (viewedCars.add(carId.toString())) {
            // Only persist if the set changed (car wasn't already viewed)
            // Limit size to prevent unbounded growth
            if (viewedCars.size > MAX_VIEWED_CARS) {
                // Remove oldest entries (first items in the list)
                val trimmed = viewedCars.drop(viewedCars.size - MAX_VIEWED_CARS).toSet()
                viewedCarsCache = trimmed.toMutableSet()
            }
            persistViewedCars(viewedCars)
        }
    }
    
    /**
     * Retrieves the list of viewed car IDs.
     */
    fun getViewedCars(): List<String> {
        return getViewedCarsInternal().toList()
    }
    
    /**
     * Internal method to get viewed cars with caching.
     */
    private fun getViewedCarsInternal(): MutableSet<String> {
        if (viewedCarsCache == null) {
            val serialized = settings.getStringOrNull(KEY_VIEWED_CARS)
            viewedCarsCache = if (serialized != null) {
                try {
                    json.decodeFromString<List<String>>(serialized).toMutableSet()
                } catch (e: Exception) {
                    mutableSetOf()
                }
            } else {
                mutableSetOf()
            }
        }
        return viewedCarsCache!!
    }
    
    /**
     * Persists viewed cars to storage.
     */
    private fun persistViewedCars(viewedCars: Set<String>) {
        val serialized = json.encodeToString(viewedCars.toList())
        settings[KEY_VIEWED_CARS] = serialized
    }
    
    /**
     * Clears the viewed cars list.
     */
    fun clearViewedCars() {
        viewedCarsCache = null
        settings.remove(KEY_VIEWED_CARS)
    }
    
    /**
     * Saves user preference.
     */
    fun savePreference(key: String, value: String) {
        settings["${KEY_USER_PREFERENCES}_$key"] = value
    }
    
    /**
     * Gets user preference.
     */
    fun getPreference(key: String): String? {
        return settings.getStringOrNull("${KEY_USER_PREFERENCES}_$key")
    }
    
    /**
     * Clears all stored app data.
     */
    fun clearAll() {
        viewedCarsCache = null
        clearViewedCars()
        // Clear other data as needed
    }
}
