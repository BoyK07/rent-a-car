package dev.koenv.rentmycar.shared.repository

import dev.koenv.rentmycar.shared.api.UserApi
import dev.koenv.rentmycar.shared.dto.user.UserDto
import kotlin.uuid.Uuid

/**
 * Repository for user operations.
 * Manages user profile data and admin operations.
 */
class UserRepository(
    private val userApi: UserApi
) {
    /**
     * Fetches a user's profile by their ID.
     * Typically called with the current user's ID.
     */
    suspend fun getUser(userId: Uuid): Result<UserDto> {
        return userApi.getCurrentUser(userId)
    }
    
    /**
     * Fetches all users (admin only).
     */
    suspend fun getAllUsers(): Result<List<UserDto>> {
        return userApi.getAllUsers()
    }
    
    /**
     * Deletes a user by ID (admin only).
     */
    suspend fun deleteUser(userId: Uuid): Result<Unit> {
        return userApi.deleteUser(userId)
    }
}
