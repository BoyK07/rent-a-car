package dev.koenv.rentmycar.shared.api

import dev.koenv.rentmycar.shared.dto.user.UserDto
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlin.uuid.Uuid

/**
 * API client for user endpoints.
 */
class UserApi(
    private val httpClient: HttpClient
) {
    /**
     * Fetches the current user's profile by their ID.
     * Note: The server requires the user ID, not a "me" endpoint.
     */
    suspend fun getCurrentUser(userId: Uuid): Result<UserDto> {
        return try {
            val response = httpClient.get("/api/v1/users/$userId")
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Fetches all users (admin only).
     */
    suspend fun getAllUsers(): Result<List<UserDto>> {
        return try {
            val response = httpClient.get("/api/v1/users")
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Deletes a user by ID (admin only).
     */
    suspend fun deleteUser(userId: Uuid): Result<Unit> {
        return try {
            httpClient.delete("/api/v1/users/$userId")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
