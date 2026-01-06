package dev.koenv.rentmycar.shared.repository

import dev.koenv.rentmycar.shared.api.UserApi
import dev.koenv.rentmycar.shared.db.dao.UserDao
import dev.koenv.rentmycar.shared.dto.user.CreateUserRequestDto
import dev.koenv.rentmycar.shared.dto.user.PatchUserRequestDto
import dev.koenv.rentmycar.shared.dto.user.UpdateUserRequestDto
import dev.koenv.rentmycar.shared.dto.user.UserDto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

/**
 * Repository for user operations with local caching.
 * Implements offline-first pattern for user data.
 */
class UserRepository(
    private val userApi: UserApi,
    private val userDao: UserDao
) {
    // Coroutine scope for background operations
    private val backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    /**
     * Fetches a user's profile by their ID.
     * Offline-first: checks database first, then API.
     */
    suspend fun getUser(userId: Uuid): Result<UserDto> {
        // Check database first
        val cachedUser = userDao.getUserById(userId)
        if (cachedUser != null) {
            // Start background refresh
            backgroundScope.launch {
                syncUserInBackground(userId)
            }
            return Result.success(cachedUser)
        }
        
        // Not in cache - fetch from API
        return userApi.getCurrentUser(userId).onSuccess { user ->
            // Cache to database
            userDao.insertOrUpdate(user)
        }
    }
    
    /**
     * Background sync for single user.
     */
    private suspend fun syncUserInBackground(userId: Uuid) {
        try {
            userApi.getCurrentUser(userId).onSuccess { user ->
                userDao.insertOrUpdate(user)
            }
        } catch (e: Exception) {
            // Silently fail background sync
        }
    }
    
    /**
     * Fetches all users (admin only).
     * Offline-first: returns cached data immediately, then syncs.
     */
    suspend fun getAllUsers(forceRefresh: Boolean = false): Result<List<UserDto>> {
        // If not forcing refresh and we have cached data, return it
        if (!forceRefresh) {
            val cachedUsers = userDao.getAllUsers()
            if (cachedUsers.isNotEmpty()) {
                // Start background sync
                backgroundScope.launch {
                    syncUsersInBackground()
                }
                return Result.success(cachedUsers)
            }
        }
        
        // No cached data or forced refresh - fetch from API
        return userApi.getAllUsers().onSuccess { users ->
            // Cache to database
            userDao.insertOrUpdateAll(users)
        }.onFailure {
            // On error, return cached data as fallback
            val cachedUsers = userDao.getAllUsers()
            if (cachedUsers.isNotEmpty()) {
                return Result.success(cachedUsers)
            }
        }
    }
    
    /**
     * Background sync for all users.
     */
    private suspend fun syncUsersInBackground() {
        try {
            userApi.getAllUsers().onSuccess { users ->
                userDao.insertOrUpdateAll(users)
            }
        } catch (e: Exception) {
            // Silently fail background sync
        }
    }
    
    /**
     * Get all users as Flow (reactive).
     */
    fun getAllUsersFlow(): Flow<List<UserDto>> {
        return userDao.getAllUsersFlow()
    }
    
    /**
     * Get user by ID as Flow (reactive).
     */
    fun getUserFlow(userId: Uuid): Flow<UserDto?> {
        return userDao.getUserByIdFlow(userId)
    }
    
    /**
     * Creates a new user (admin only).
     */
    suspend fun createUser(request: CreateUserRequestDto): Result<UserDto> {
        return userApi.createUser(request).onSuccess { user ->
            // Add to database immediately
            userDao.insertOrUpdate(user)
        }
    }
    
    /**
     * Updates an existing user (admin only).
     */
    suspend fun updateUser(userId: Uuid, request: UpdateUserRequestDto): Result<UserDto> {
        return userApi.updateUser(userId, request).onSuccess { user ->
            // Update in database
            userDao.insertOrUpdate(user)
        }
    }
    
    /**
     * Partially updates a user (admin only).
     */
    suspend fun patchUser(userId: Uuid, request: PatchUserRequestDto): Result<UserDto> {
        return userApi.patchUser(userId, request).onSuccess { user ->
            // Update in database
            userDao.insertOrUpdate(user)
        }
    }
    
    /**
     * Deletes a user by ID (admin only).
     */
    suspend fun deleteUser(userId: Uuid): Result<Unit> {
        return userApi.deleteUser(userId).onSuccess {
            // Remove from database
            userDao.deleteById(userId)
        }
    }
}
