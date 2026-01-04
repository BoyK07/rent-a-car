package dev.koenv.rentmycar.shared.api

import dev.koenv.rentmycar.shared.dto.user.UserDto
import dev.koenv.rentmycar.shared.http.ApiResponse
import dev.koenv.rentmycar.shared.resources.ApiV1
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.resources.*
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
            val response = httpClient.get(ApiV1.Users.Id(id = userId.toString()))
            val apiResponse = response.body<ApiResponse<UserDto>>()
            if (apiResponse.success && apiResponse.data != null) {
                Result.success(apiResponse.data)
            } else {
                Result.failure(Exception(apiResponse.message ?: "Failed to fetch user"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Fetches all users (admin only).
     */
    suspend fun getAllUsers(): Result<List<UserDto>> {
        return try {
            val response = httpClient.get(ApiV1.Users())
            val apiResponse = response.body<ApiResponse<List<UserDto>>>()
            if (apiResponse.success && apiResponse.data != null) {
                Result.success(apiResponse.data)
            } else {
                Result.failure(Exception(apiResponse.message ?: "Failed to fetch users"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Deletes a user by ID (admin only).
     */
    suspend fun deleteUser(userId: Uuid): Result<Unit> {
        return try {
            val response = httpClient.delete(ApiV1.Users.Id(id = userId.toString()))
            val apiResponse = response.body<ApiResponse<Unit>>()
            if (apiResponse.success) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(apiResponse.message ?: "Failed to delete user"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

