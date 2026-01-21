package dev.koenv.rentmycar.server.domain.service

import dev.koenv.rentmycar.server.storage.repository.CarPhotoRepositoryImpl
import dev.koenv.rentmycar.shared.domain.entity.CarPhoto
import kotlin.uuid.Uuid

/**
 * Service layer for managing car photos.
 * 
 * Handles CRUD operations for car images/photos in the system.
 * Each car can have multiple photos, with one marked as primary
 * for thumbnail/preview display.
 * 
 * @property repo The car photo repository for data persistence
 */
class CarPhotoService(private val repo: CarPhotoRepositoryImpl) {
    
    /**
     * Retrieves all car photos in the system.
     * 
     * @return List of all car photos
     */
    suspend fun getAll(): List<CarPhoto> = repo.findAll()
    
    /**
     * Finds a specific car photo by its unique identifier.
     * 
     * @param id The UUID of the photo to retrieve
     * @return The car photo if found, null otherwise
     */
    suspend fun getById(id: Uuid): CarPhoto? = repo.findById(id)
    
    /**
     * Retrieves all photos associated with a specific car.
     * 
     * Useful for displaying a gallery of car images.
     * The primary photo is typically shown first in the UI.
     * 
     * @param carId The UUID of the car whose photos to retrieve
     * @return List of photos for the specified car, may be empty
     */
    suspend fun getByCarId(carId: Uuid): List<CarPhoto> = repo.findByCarId(carId)
    
    /**
     * Adds a new photo to a car.
     * 
     * @param photo The photo entity to create (must include carId and URL)
     * @return The created photo with generated ID
     */
    suspend fun create(photo: CarPhoto): CarPhoto = repo.create(photo)
    
    /**
     * Updates an existing car photo.
     * 
     * Typically used to change the primary photo flag or update the URL.
     * 
     * @param id The UUID of the photo to update
     * @param photo The updated photo data
     * @return The updated photo if found, null if photo doesn't exist
     */
    suspend fun update(id: Uuid, photo: CarPhoto): CarPhoto? = repo.update(id, photo)
    
    /**
     * Deletes a car photo from the system.
     * 
     * Note: This only removes the database record. The actual image file
     * should be deleted from storage separately if needed.
     * 
     * @param id The UUID of the photo to delete
     * @return true if photo was deleted, false if photo didn't exist
     */
    suspend fun delete(id: Uuid): Boolean = repo.delete(id)
}
