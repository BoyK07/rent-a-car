package dev.koenv.rentmycar.server.domain.service

import dev.koenv.rentmycar.server.storage.repository.UserRepositoryImpl
import dev.koenv.rentmycar.shared.domain.entity.User
import kotlin.uuid.Uuid

/**
 * Service layer for user management operations.
 * 
 * Provides business logic for user CRUD operations and queries.
 * Acts as an intermediary between route handlers and the data layer.
 * 
 * @property repo The user repository for data persistence operations
 */
class UserService(private val repo: UserRepositoryImpl) {
    
    /**
     * Retrieves all users from the system.
     * 
     * @return List of all users
     */
    suspend fun getAll(): List<User> = repo.findAll()
    
    /**
     * Finds a user by their unique identifier.
     * 
     * @param id The UUID of the user to retrieve
     * @return The user if found, null otherwise
     */
    suspend fun getById(id: Uuid): User? = repo.findById(id)
    
    /**
     * Creates a new user in the system.
     * 
     * Note: Password hashing and validation should be handled
     * before calling this method (typically in AuthService).
     * 
     * @param user The user entity to create
     * @return The created user with generated ID
     */
    suspend fun create(user: User): User = repo.create(user)
    
    /**
     * Updates an existing user's information.
     * 
     * @param id The UUID of the user to update
     * @param user The updated user data
     * @return The updated user if found, null if user doesn't exist
     */
    suspend fun update(id: Uuid, user: User): User? = repo.update(id, user)
    
    /**
     * Deletes a user from the system.
     * 
     * @param id The UUID of the user to delete
     * @return true if user was deleted, false if user didn't exist
     */
    suspend fun delete(id: Uuid): Boolean = repo.delete(id)
    
    /**
     * Finds a user by their email address.
     * 
     * Used primarily for authentication and duplicate email checks.
     * 
     * @param email The email address to search for
     * @return The user if found, null otherwise
     */
    suspend fun findByEmail(email: String): User? = repo.findByEmail(email)
}
